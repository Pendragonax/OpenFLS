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
# Locks älter als diese Grenze gelten als verwaist (z. B. nach einem harten
# Container-Absturz mitten im Dump) und werden gebrochen. Standard: 12 Stunden.
BACKUP_STALE_LOCK_SECONDS="${BACKUP_STALE_LOCK_SECONDS:-43200}"
case "$BACKUP_STALE_LOCK_SECONDS" in
  *[!0-9]*|'') echo 'BACKUP_STALE_LOCK_SECONDS must be a positive integer' >&2; exit 2 ;;
esac
success=false
lock_acquired=false

# Gibt datensparsame, strukturierte Ereignisse für Docker-Logs und Monitoring aus.
json_log() {
  local level="$1"
  local event_name="$2"
  local outcome="$3"
  local message="$4"
  printf '{"timestamp":"%s","level":"%s","event_name":"%s","outcome":"%s","service":"openfls-backup","message":"%s","run_id":"%s"}\n' \
    "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" "$level" "$event_name" "$outcome" "$message" "$run_id"
}

# Klassifiziert einen mysql-/mysqldump-Fehler grob, ohne Klartext oder
# Zugangsdaten zu übernehmen. Der Wert steuert in OpenFLS einen konkreten
# Handlungshinweis.
classify_mysql_error() {
  local error_file="$1"
  # Reihenfolge zählt: die spezifischen Rechte-Fehler (1044/114x, die den Text
  # "Access denied ... to database" enthalten) vor dem generischen 1045.
  if grep -qE 'ERROR (200[0-9]|1130)|[Cc]an.?t connect|Unknown MySQL server host|Unknown database' "$error_file" 2>/dev/null; then
    echo database_unreachable
  elif grep -qE 'ERROR (1044|114[0-9])|command denied to user|Access denied for user .* to database' "$error_file" 2>/dev/null; then
    echo insufficient_grants
  elif grep -qE 'ERROR 1045|Access denied for user' "$error_file" 2>/dev/null; then
    echo backup_user_missing
  else
    echo unknown
  fi
}

# Aktualisiert atomar den letzten Status und ergänzt den begrenzten technischen Verlauf.
# $3 size_bytes, $4 sha256, $5 reason (nur bei Fehlern gesetzt).
write_status() {
  local outcome="$1"
  local message="$2"
  local finished_at duration_seconds size_bytes sha256 reason
  finished_at="$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')"
  duration_seconds="$(( $(date +%s) - started_epoch ))"
  size_bytes="${3:-0}"
  sha256="${4:-}"
  reason="${5:-}"

  # Kompakte JSON-Zeile ohne abschließenden Zeilenumbruch (den entfernt $(...)).
  # schema_version erlaubt spätere Migrationen, wenn sich das Format ändert.
  local status_line
  status_line=$(printf '{"schema_version":1,"timestamp":"%s","level":"%s","event_name":"backup.completed","outcome":"%s","service":"openfls-backup","message":"%s","run_id":"%s","database":"%s","backup_file":"%s","size_bytes":%s,"sha256":"%s","duration_seconds":%s,"reason":"%s"}' \
    "$finished_at" "$( [ "$outcome" = success ] && echo INFO || echo ERROR )" "$outcome" "$message" "$run_id" "$MYSQL_DATABASE" "$(basename "$backup_file")" "$size_bytes" "$sha256" "$duration_seconds" "$reason")

  printf '%s\n' "$status_line" > "$STATUS_DIR/latest.json.tmp"
  mv -f "$STATUS_DIR/latest.json.tmp" "$STATUS_DIR/latest.json"
  # Jede History-Zeile MUSS mit \n abgeschlossen sein, sonst kleben die Einträge
  # aneinander und Leser sehen nur den ersten.
  printf '%s\n' "$status_line" >> "$STATUS_DIR/history.jsonl"
  tail -n "$BACKUP_HISTORY_MAX_ENTRIES" "$STATUS_DIR/history.jsonl" > "$STATUS_DIR/history.jsonl.tmp"
  mv -f "$STATUS_DIR/history.jsonl.tmp" "$STATUS_DIR/history.jsonl"
}

# Entfernt unvollständige Dateien und gibt nur die selbst gehaltene Sperre frei.
cleanup() {
  rm -f -- "$temporary_file" "${dump_error_file:-}" "${preflight_error_file:-}"
  if $lock_acquired; then
    rm -rf -- "$lock_dir" 2>/dev/null || true
  fi
}

# Versucht die Sperre zu übernehmen und bricht dabei nur nachweislich verwaiste
# Sperren eines abgestürzten Vorläufers, niemals die eines laufenden Jobs.
acquire_lock() {
  if mkdir "$lock_dir" 2>/dev/null; then
    date +%s > "$lock_dir/started_at" 2>/dev/null || true
    lock_acquired=true
    return 0
  fi

  local lock_epoch now_epoch
  lock_epoch="$(cat "$lock_dir/started_at" 2>/dev/null || true)"
  case "$lock_epoch" in
    ''|*[!0-9]*) lock_epoch="$(stat -c '%Y' "$lock_dir" 2>/dev/null || true)" ;;
  esac
  now_epoch="$(date +%s)"
  case "$lock_epoch" in
    ''|*[!0-9]*) return 1 ;;
  esac
  if [ "$(( now_epoch - lock_epoch ))" -lt "$BACKUP_STALE_LOCK_SECONDS" ]; then
    return 1
  fi

  json_log WARN backup.lock.stale_broken denied 'Breaking a stale backup lock from a crashed run'
  rm -rf -- "$lock_dir" 2>/dev/null || true
  if mkdir "$lock_dir" 2>/dev/null; then
    date +%s > "$lock_dir/started_at" 2>/dev/null || true
    lock_acquired=true
    return 0
  fi
  return 1
}

on_error() {
  local exit_code="$1"
  if ! $success; then
    write_status failure 'Backup job failed before a verified dump was created' '' '' unknown || true
    json_log ERROR backup.failed failure 'Backup job failed before a verified dump was created' || true
  fi
  exit "$exit_code"
}

# Registriert Aufräumen und Fehlerstatus, bevor der eigentliche Backup-Lauf beginnt.
started_epoch="$(date +%s)"
trap cleanup EXIT
trap 'on_error $?' ERR

# Bereitet die geschützte Ablage vor. Ein bereits von der Einrichtung angelegtes
# Verzeichnis mit abweichenden Rechten darf den Lauf nicht hart abbrechen.
mkdir -p "$BACKUP_DIR" "$STATUS_DIR"
chmod 700 "$BACKUP_DIR" "$STATUS_DIR" 2>/dev/null || true

# Verhindert parallele Dump-Läufe; die Sperre wird erst nach Übernahme aufgeräumt.
if ! acquire_lock; then
  json_log WARN backup.skipped denied 'Another backup job is already running'
  exit 0
fi

# Liest das dedizierte Backup-Secret, ohne es in Logs oder Prozessargumente zu schreiben.
if ! [ -r "$MYSQL_PASSWORD_FILE" ]; then
  write_status failure 'Backup password secret is not readable' '' '' backup_secret_missing
  json_log ERROR backup.failed failure 'Backup password secret is not readable'
  exit 2
fi
export MYSQL_PWD
MYSQL_PWD="$(<"$MYSQL_PASSWORD_FILE")"

# Prüft die Anmeldung des Backup-Kontos explizit vor dem Dump. So liefert ein
# fehlender oder falsch konfigurierter Benutzer einen eindeutigen Grund
# (backup_user_missing) statt eines generischen mysqldump-Fehlers - OpenFLS
# verweist dann direkt auf scripts/database_create_backup_user.sh.
# Der Fehler wird bewusst mit "|| var=$?" abgefangen: nur so überspringt bash
# den ERR-Trap zuverlässig - "set +e" allein reicht dafür nicht.
preflight_error_file="$STATUS_DIR/.preflight.err"
preflight_exit_code=0
mysql \
  --host="$MYSQL_HOST" \
  --port="$MYSQL_PORT" \
  --user="$MYSQL_USER" \
  --connect-timeout=10 \
  --batch --skip-column-names \
  -e 'SELECT 1' >/dev/null 2>"$preflight_error_file" || preflight_exit_code=$?

if [ "$preflight_exit_code" -ne 0 ]; then
  preflight_reason="$(classify_mysql_error "$preflight_error_file")"
  rm -f -- "$preflight_error_file"
  case "$preflight_reason" in
    backup_user_missing)
      preflight_message="Backup database user '$MYSQL_USER' is missing or its password does not match - run scripts/database_create_backup_user.sh" ;;
    database_unreachable)
      preflight_message="Database is not reachable for the backup service" ;;
    insufficient_grants)
      preflight_message="Backup user '$MYSQL_USER' cannot log in - run scripts/database_create_backup_user.sh" ;;
    *)
      preflight_message="Backup connection pre-check failed before the dump started" ;;
  esac
  write_status failure "$preflight_message" '' '' "$preflight_reason"
  json_log ERROR backup.failed failure "$preflight_message"
  exit 1
fi
rm -f -- "$preflight_error_file"

# Erstellt einen konsistenten InnoDB-Dump, komprimiert ihn und schreibt zunächst nur eine temporäre Datei.
# Der mysqldump-Fehlerkanal wird getrennt aufgefangen, um die Ursache zu klassifizieren.
json_log INFO backup.started unknown 'Creating logical MySQL backup'
dump_error_file="$STATUS_DIR/.dump.err"
# "|| dump_exit_code=$?" markiert die Pipeline als behandelt, damit der ERR-Trap
# nicht vorher zuschlägt. pipefail liefert dabei den mysqldump-Exitcode.
dump_exit_code=0
mysqldump \
  --host="$MYSQL_HOST" \
  --port="$MYSQL_PORT" \
  --user="$MYSQL_USER" \
  --single-transaction \
  --triggers \
  --no-tablespaces \
  --set-gtid-purged=OFF \
  "$MYSQL_DATABASE" 2>"$dump_error_file" \
  | gzip -c > "$temporary_file" || dump_exit_code=$?

if [ "$dump_exit_code" -ne 0 ]; then
  dump_reason="$(classify_mysql_error "$dump_error_file")"
  rm -f -- "$dump_error_file"
  case "$dump_reason" in
    backup_user_missing) dump_message='mysqldump denied: backup database user missing or password mismatch' ;;
    database_unreachable) dump_message='mysqldump could not reach the database' ;;
    insufficient_grants) dump_message='mysqldump denied: backup user lacks the required privileges' ;;
    *) dump_message='mysqldump failed before a verified dump was created' ;;
  esac
  write_status failure "$dump_message" '' '' "$dump_reason"
  json_log ERROR backup.failed failure "$dump_message"
  exit 1
fi
rm -f -- "$dump_error_file"

# Veröffentlicht erst den vollständigen Dump, erzeugt dessen Prüfsumme und technische Metadaten.
mv -f "$temporary_file" "$backup_file"
(cd "$BACKUP_DIR" && sha256sum "$(basename "$backup_file")" > "$(basename "$checksum_file")")
size_bytes="$(stat -c '%s' "$backup_file")"
sha256="$(awk 'NR == 1 { print $1 }' "$checksum_file")"
finished_at="$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')"
duration_seconds="$(( $(date +%s) - started_epoch ))"
printf '{"schema_version":1,"timestamp":"%s","level":"INFO","event_name":"backup.created","outcome":"success","service":"openfls-backup","run_id":"%s","database":"%s","backup_file":"%s","size_bytes":%s,"sha256":"%s","duration_seconds":%s}\n' \
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
