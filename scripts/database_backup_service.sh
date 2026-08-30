#!/usr/bin/env bash
# Hält den Backup-Container am Leben und plant regelmäßige Einmal-Backups.

set -Eeuo pipefail
umask 077

# Nach einem Fehler wird schneller wiederholt als nach einem Erfolg.
BACKUP_INTERVAL_SECONDS="${BACKUP_INTERVAL_SECONDS:-21600}"
BACKUP_RETRY_INTERVAL_SECONDS="${BACKUP_RETRY_INTERVAL_SECONDS:-300}"

# Weitere Betriebsparameter nur für die veröffentlichte Konfigurationsdatei.
BACKUP_DIR="${BACKUP_DIR:-/backup}"
STATUS_DIR="${BACKUP_STATUS_DIR:-$BACKUP_DIR/status}"
MYSQL_DATABASE="${MYSQL_DATABASE:-openfls}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"
BACKUP_HISTORY_MAX_ENTRIES="${BACKUP_HISTORY_MAX_ENTRIES:-1000}"
BACKUP_MAX_AGE_HOURS="${BACKUP_MAX_AGE_HOURS:-7}"
BACKUP_STALE_LOCK_SECONDS="${BACKUP_STALE_LOCK_SECONDS:-43200}"

# Verhindert Endlosschleifen durch ungültige Zeitangaben.
for value_name in BACKUP_INTERVAL_SECONDS BACKUP_RETRY_INTERVAL_SECONDS \
  BACKUP_RETENTION_DAYS BACKUP_HISTORY_MAX_ENTRIES BACKUP_MAX_AGE_HOURS BACKUP_STALE_LOCK_SECONDS; do
  case "${!value_name}" in
    *[!0-9]*|'') echo "$value_name must be a non-negative integer" >&2; exit 2 ;;
  esac
done

# Schreibt datensparsame, strukturierte Betriebsereignisse in die Container-Logs.
log() {
  printf '{"timestamp":"%s","level":"%s","event_name":"%s","outcome":"%s","service":"openfls-backup","message":"%s"}\n' \
    "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" "$1" "$2" "$3" "$4"
}

# Veröffentlicht die effektive Konfiguration für die OpenFLS-Oberfläche. Enthält
# nur Betriebsparameter, keine Zugangsdaten und keine Fachdaten.
write_config() {
  mkdir -p "$STATUS_DIR"
  local config_file="$STATUS_DIR/config.json"
  printf '{"schema_version":1,"database":"%s","interval_seconds":%s,"retry_interval_seconds":%s,"retention_days":%s,"history_max_entries":%s,"max_age_hours":%s,"stale_lock_seconds":%s,"generated_at":"%s"}\n' \
    "$MYSQL_DATABASE" "$BACKUP_INTERVAL_SECONDS" "$BACKUP_RETRY_INTERVAL_SECONDS" \
    "$BACKUP_RETENTION_DAYS" "$BACKUP_HISTORY_MAX_ENTRIES" "$BACKUP_MAX_AGE_HOURS" \
    "$BACKUP_STALE_LOCK_SECONDS" "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" \
    > "$config_file.tmp"
  mv -f "$config_file.tmp" "$config_file"
}

write_config

# Der Job liegt im selben (schreibgeschützt gemounteten) Skriptverzeichnis.
JOB_SCRIPT="$(cd "$(dirname "$0")" && pwd)/database_backup_job.sh"

# Führt sofort einen Lauf aus und wartet anschließend abhängig vom Ergebnis.
while true; do
  if bash "$JOB_SCRIPT"; then
    log INFO backup.scheduler.wait success 'Backup finished; waiting for next scheduled run'
    sleep "$BACKUP_INTERVAL_SECONDS"
  else
    log ERROR backup.scheduler.retry failure 'Backup failed; waiting before retry'
    sleep "$BACKUP_RETRY_INTERVAL_SECONDS"
  fi
done
