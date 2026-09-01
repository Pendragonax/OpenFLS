#!/usr/bin/env bash
# Haelt den Backup-Container am Leben und startet zur konfigurierten Uhrzeit ein
# Backup, sofern seit dem letzten Erfolg mindestens BACKUP_INTERVAL_DAYS
# Kalendertage vergangen sind. Bei Fehlern wird bis zum Erfolg wiederholt.

set -Eeuo pipefail
umask 077

BACKUP_TIME="${BACKUP_TIME:-02:30}"
BACKUP_TIMEZONE="${BACKUP_TIMEZONE:-Europe/Berlin}"
# Abstand in ganzen Tagen zwischen zwei Backups: 1 = taeglich, 2 = alle 48 h usw.
# Werte < 1 oder ungueltige Werte werden als 1 interpretiert.
BACKUP_INTERVAL_DAYS="${BACKUP_INTERVAL_DAYS:-1}"
BACKUP_RETRY_INTERVAL_SECONDS="${BACKUP_RETRY_INTERVAL_SECONDS:-300}"

# Weitere Betriebsparameter nur fuer die veroeffentlichte Konfigurationsdatei.
BACKUP_DIR="${BACKUP_DIR:-/backup}"
STATUS_DIR="${BACKUP_STATUS_DIR:-$BACKUP_DIR/status}"
MYSQL_DATABASE="${MYSQL_DATABASE:-openfls}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"
BACKUP_HISTORY_MAX_ENTRIES="${BACKUP_HISTORY_MAX_ENTRIES:-1000}"
BACKUP_MAX_AGE_HOURS="${BACKUP_MAX_AGE_HOURS:-26}"
BACKUP_STALE_LOCK_SECONDS="${BACKUP_STALE_LOCK_SECONDS:-43200}"

# Alle lokalen date-Aufrufe rechnen in dieser Zone; UTC-Zeitstempel nutzen date -u.
export TZ="$BACKUP_TIMEZONE"

# BACKUP_TIME muss HH:MM im Bereich 00:00-23:59 sein.
case "$BACKUP_TIME" in
  [0-2][0-9]:[0-5][0-9]) : ;;
  *) echo 'BACKUP_TIME must be HH:MM (00:00-23:59)' >&2; exit 2 ;;
esac
if [ "$(( 10#${BACKUP_TIME%%:*} ))" -gt 23 ]; then
  echo 'BACKUP_TIME hour must be 00-23' >&2; exit 2
fi

# BACKUP_INTERVAL_DAYS auf eine ganze Zahl >= 1 normalisieren.
case "$BACKUP_INTERVAL_DAYS" in *[!0-9]*|'') BACKUP_INTERVAL_DAYS=1 ;; esac
[ "$BACKUP_INTERVAL_DAYS" -ge 1 ] || BACKUP_INTERVAL_DAYS=1

for value_name in BACKUP_RETRY_INTERVAL_SECONDS BACKUP_RETENTION_DAYS \
  BACKUP_HISTORY_MAX_ENTRIES BACKUP_MAX_AGE_HOURS BACKUP_STALE_LOCK_SECONDS; do
  case "${!value_name}" in
    *[!0-9]*|'') echo "$value_name must be a non-negative integer" >&2; exit 2 ;;
  esac
done

# Schreibt datensparsame, strukturierte Betriebsereignisse in die Container-Logs.
log() {
  printf '{"timestamp":"%s","level":"%s","event_name":"%s","outcome":"%s","service":"openfls-backup","message":"%s"}\n' \
    "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" "$1" "$2" "$3" "$4"
}

# Veroeffentlicht die effektive Konfiguration fuer die OpenFLS-Oberflaeche.
# Enthaelt nur Betriebsparameter, keine Zugangsdaten und keine Fachdaten.
write_config() {
  mkdir -p "$STATUS_DIR"
  local config_file="$STATUS_DIR/config.json"
  printf '{"schema_version":1,"database":"%s","backup_time":"%s","timezone":"%s","interval_days":%s,"retry_interval_seconds":%s,"retention_days":%s,"history_max_entries":%s,"max_age_hours":%s,"stale_lock_seconds":%s,"generated_at":"%s"}\n' \
    "$MYSQL_DATABASE" "$BACKUP_TIME" "$BACKUP_TIMEZONE" "$BACKUP_INTERVAL_DAYS" "$BACKUP_RETRY_INTERVAL_SECONDS" \
    "$BACKUP_RETENTION_DAYS" "$BACKUP_HISTORY_MAX_ENTRIES" "$BACKUP_MAX_AGE_HOURS" \
    "$BACKUP_STALE_LOCK_SECONDS" "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" \
    > "$config_file.tmp"
  mv -f "$config_file.tmp" "$config_file"
}

# Sekunden bis zum naechsten Auftreten von BACKUP_TIME (heute, sonst morgen).
seconds_until_backup_time() {
  local now target
  now="$(date +%s)"
  target="$(date -d "today $BACKUP_TIME" +%s)"
  [ "$target" -gt "$now" ] || target="$(date -d "tomorrow $BACKUP_TIME" +%s)"
  echo "$(( target - now ))"
}

# true, wenn seit dem letzten erfolgreichen Backup mindestens
# BACKUP_INTERVAL_DAYS Kalendertage (in BACKUP_TIMEZONE) vergangen sind - bzw.
# noch kein erfolgreiches Backup vorliegt.
backup_is_due() {
  local latest="$STATUS_DIR/latest.json" timestamp last_date today last_noon today_noon
  [ -r "$latest" ] || return 0
  grep -q '"outcome":"success"' "$latest" || return 0
  timestamp="$(sed -n 's/.*"timestamp":"\([^"]*\)".*/\1/p' "$latest")"
  [ -n "$timestamp" ] || return 0
  last_date="$(date -d "$timestamp" +%Y-%m-%d 2>/dev/null)" || return 0
  today="$(date +%Y-%m-%d)"
  # Beide Datumsgrenzen auf 12:00 UTC verankern, damit die Differenz DST-unabhaengig
  # ein glattes Vielfaches von 86400 ist.
  last_noon="$(date -u -d "${last_date}T12:00:00Z" +%s 2>/dev/null)" || return 0
  today_noon="$(date -u -d "${today}T12:00:00Z" +%s)"
  [ "$(( (today_noon - last_noon) / 86400 ))" -ge "$BACKUP_INTERVAL_DAYS" ]
}

# Fuehrt den Einmal-Job aus und wiederholt bei Fehler bis zum Erfolg.
run_backup_with_retry() {
  until bash "$JOB_SCRIPT"; do
    log ERROR backup.scheduler.retry failure "Backup failed; retrying in ${BACKUP_RETRY_INTERVAL_SECONDS}s"
    sleep "$BACKUP_RETRY_INTERVAL_SECONDS"
  done
}

write_config

# Der Job liegt im selben (schreibgeschuetzt gemounteten) Skriptverzeichnis.
JOB_SCRIPT="$(cd "$(dirname "$0")" && pwd)/database_backup_job.sh"

# Beim Start einmal nachholen, falls seit dem letzten Erfolg das Intervall
# ueberschritten ist (oder es noch kein Backup gibt).
if backup_is_due; then
  log INFO backup.scheduler.catchup unknown 'Backup is due; running one now'
  run_backup_with_retry
fi

# Taeglich zur konfigurierten Uhrzeit pruefen und bei Faelligkeit sichern.
while true; do
  wait_seconds="$(seconds_until_backup_time)"
  log INFO backup.scheduler.wait success "Next check at ${BACKUP_TIME} ${BACKUP_TIMEZONE} in ${wait_seconds}s (every ${BACKUP_INTERVAL_DAYS} day(s))"
  sleep "$wait_seconds"
  if backup_is_due; then
    run_backup_with_retry
  else
    log INFO backup.scheduler.skipped success "Backup not due yet; interval is ${BACKUP_INTERVAL_DAYS} day(s)"
  fi
done
