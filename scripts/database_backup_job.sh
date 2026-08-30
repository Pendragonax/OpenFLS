#!/usr/bin/env bash
# Erstellt genau einen konsistenten, komprimierten MySQL-Dump mit Statusdatei.

set -Eeuo pipefail

# Neue Dump-, Status- und Metadatendateien sind nur für den Backup-Account lesbar.
umask 077

# Liest alle Betriebsparameter aus der Compose-Umgebung mit sicheren Standardwerten.
BACKUP_DIR="${BACKUP_DIR:-/backup}"
STATUS_DIR="${BACKUP_STATUS_DIR:-$BACKUP_DIR/status}"
MYSQL_HOST="${MYSQL_HOST:-db}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:?MYSQL_DATABASE must be set}"
MYSQL_USER="${MYSQL_BACKUP_USER:-openfls_backup}"
MYSQL_PASSWORD_FILE="${MYSQL_BACKUP_PASSWORD_FILE:-/run/secrets/db_backup_password}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"
BACKUP_HISTORY_MAX_ENTRIES="${BACKUP_HISTORY_MAX_ENTRIES:-1000}"

# Prüft Konfigurationswerte früh, damit keine unsicheren Dateinamen oder SQL-Ziele entstehen.
case "$MYSQL_DATABASE" in
  *[!A-Za-z0-9_]*|'') echo 'MYSQL_DATABASE must only contain letters, digits, and underscores' >&2; exit 2 ;;
esac
case "$BACKUP_RETENTION_DAYS" in
  *[!0-9]*|'') echo 'BACKUP_RETENTION_DAYS must be a non-negative integer' >&2; exit 2 ;;
esac
case "$BACKUP_HISTORY_MAX_ENTRIES" in
  *[!0-9]*|'') echo 'BACKUP_HISTORY_MAX_ENTRIES must be a positive integer' >&2; exit 2 ;;
esac

# Leitet eindeutige Namen für Dump, Prüfsumme und Metadaten aus dem Startzeitpunkt ab.
started_at="$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')"
run_id="$(date -u +'%Y%m%dT%H%M%SZ')-${MYSQL_DATABASE}"
backup_file="$BACKUP_DIR/${run_id}.sql.gz"
checksum_file="${backup_file}.sha256"
metadata_file="$BACKUP_DIR/${run_id}.json"
temporary_file="${backup_file}.part"
lock_dir="$STATUS_DIR/.backup.lock"
success=false

# Gibt datensparsame, strukturierte Ereignisse für Docker-Logs und Monitoring aus.
json_log() {
  local level="$1"
  local event_name="$2"
  local outcome="$3"
  local message="$4"
  printf '{"timestamp":"%s","level":"%s","event_name":"%s","outcome":"%s","service":"openfls-backup","message":"%s","run_id":"%s"}\n' \
    "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" "$level" "$event_name" "$outcome" "$message" "$run_id"
}

# Aktualisiert atomar den letzten Status und ergänzt den begrenzten technischen Verlauf.
write_status() {
  local outcome="$1"
  local message="$2"
  local finished_at duration_seconds size_bytes sha256
  finished_at="$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')"
  duration_seconds="$(( $(date +%s) - started_epoch ))"
  size_bytes="${3:-0}"
  sha256="${4:-}"

  local status_line
  status_line=$(printf '{"timestamp":"%s","level":"%s","event_name":"backup.completed","outcome":"%s","service":"openfls-backup","message":"%s","run_id":"%s","database":"%s","backup_file":"%s","size_bytes":%s,"sha256":"%s","duration_seconds":%s}\n' \
    "$finished_at" "$( [ "$outcome" = success ] && echo INFO || echo ERROR )" "$outcome" "$message" "$run_id" "$MYSQL_DATABASE" "$(basename "$backup_file")" "$size_bytes" "$sha256" "$duration_seconds")

  printf '%s' "$status_line" > "$STATUS_DIR/latest.json.tmp"
  mv -f "$STATUS_DIR/latest.json.tmp" "$STATUS_DIR/latest.json"
  printf '%s' "$status_line" >> "$STATUS_DIR/history.jsonl"
  tail -n "$BACKUP_HISTORY_MAX_ENTRIES" "$STATUS_DIR/history.jsonl" > "$STATUS_DIR/history.jsonl.tmp"
  mv -f "$STATUS_DIR/history.jsonl.tmp" "$STATUS_DIR/history.jsonl"
}

# Entfernt unvollständige Dateien und gibt die Sperre auch bei Fehlern wieder frei.
cleanup() {
  rm -f -- "$temporary_file"
  rmdir "$lock_dir" 2>/dev/null || true
}

on_error() {
  local exit_code="$1"
  if ! $success; then
    write_status failure 'Backup job failed before a verified dump was created' || true
    json_log ERROR backup.failed failure 'Backup job failed before a verified dump was created' || true
  fi
  exit "$exit_code"
}

# Registriert Aufräumen und Fehlerstatus, bevor der eigentliche Backup-Lauf beginnt.
started_epoch="$(date +%s)"
trap cleanup EXIT
trap 'on_error $?' ERR

# Bereitet die geschützte Ablage vor und verhindert parallele Dump-Läufe.
mkdir -p "$BACKUP_DIR" "$STATUS_DIR"
chmod 700 "$BACKUP_DIR" "$STATUS_DIR"
if ! mkdir "$lock_dir" 2>/dev/null; then
  json_log WARN backup.skipped denied 'Another backup job is already running'
  exit 0
fi

# Liest das dedizierte Backup-Secret, ohne es in Logs oder Prozessargumente zu schreiben.
if ! [ -r "$MYSQL_PASSWORD_FILE" ]; then
  write_status failure 'Backup password secret is not readable'
  json_log ERROR backup.failed failure 'Backup password secret is not readable'
  exit 2
fi
export MYSQL_PWD
MYSQL_PWD="$(<"$MYSQL_PASSWORD_FILE")"

# Erstellt einen konsistenten InnoDB-Dump, komprimiert ihn und schreibt zunächst nur eine temporäre Datei.
json_log INFO backup.started unknown 'Creating logical MySQL backup'
mysqldump \
  --host="$MYSQL_HOST" \
  --port="$MYSQL_PORT" \
  --user="$MYSQL_USER" \
  --single-transaction \
  --triggers \
  --no-tablespaces \
  --set-gtid-purged=OFF \
  "$MYSQL_DATABASE" \
  | gzip -c > "$temporary_file"

# Veröffentlicht erst den vollständigen Dump, erzeugt dessen Prüfsumme und technische Metadaten.
mv -f "$temporary_file" "$backup_file"
(cd "$BACKUP_DIR" && sha256sum "$(basename "$backup_file")" > "$(basename "$checksum_file")")
size_bytes="$(stat -c '%s' "$backup_file")"
sha256="$(awk 'NR == 1 { print $1 }' "$checksum_file")"
finished_at="$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')"
duration_seconds="$(( $(date +%s) - started_epoch ))"
printf '{"timestamp":"%s","level":"INFO","event_name":"backup.created","outcome":"success","service":"openfls-backup","run_id":"%s","database":"%s","backup_file":"%s","size_bytes":%s,"sha256":"%s","duration_seconds":%s}\n' \
  "$finished_at" "$run_id" "$MYSQL_DATABASE" "$(basename "$backup_file")" "$size_bytes" "$sha256" "$duration_seconds" > "$metadata_file"

# Löscht ausschließlich abgelaufene, eigene Dump-Dateien samt Begleitdateien.
find "$BACKUP_DIR" -maxdepth 1 -type f -name '*.sql.gz' -mtime "+$BACKUP_RETENTION_DAYS" -print0 \
  | while IFS= read -r -d '' expired_backup; do
      rm -f -- "$expired_backup" "${expired_backup}.sha256" "${expired_backup%.sql.gz}.json"
    done

# Meldet den Erfolg erst nach vollständig erzeugtem und geprüften Dump.
success=true
write_status success 'Verified logical MySQL backup created' "$size_bytes" "$sha256"
json_log INFO backup.succeeded success 'Verified logical MySQL backup created'
