#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  scripts/database_restore.sh [options] <backup.sql.gz|backup.sql|backup.tgz>

Options:
  -y, --yes              Skip interactive confirmation
  -f, --compose-file     Compose file path (overrides COMPOSE_FILE_PATH)
      --stop-services    Deprecated (ignored; full stack restart is always used)
  --no-pre-backup    Skip safety backup before restore
  --skip-checksum    Skip SHA256 verification for .gz backups
  -h, --help             Show this help

Environment:
  COMPOSE_FILE_PATH      Compose file path (optional; if empty, you can choose interactively)
  RESTORE_STOP_SERVICES  Deprecated (ignored; full stack restart is always used)
EOF
}

log() {
  echo "[INFO] $*"
}

warn() {
  echo "[WARN] $*" >&2
}

die() {
  echo "[ERROR] $*" >&2
  exit 1
}

require_command() {
  local cmd="$1"
  command -v "$cmd" >/dev/null 2>&1 || die "required command not found: $cmd"
}

VERIFY_CHECKSUM=true
DO_PRE_BACKUP=true
ASSUME_YES=false
SOURCE_FILE=""
COMPOSE_FILE_PATH="${COMPOSE_FILE_PATH:-}"
RESTORE_STOP_SERVICES="${RESTORE_STOP_SERVICES:-}"

while [ "$#" -gt 0 ]; do
  case "$1" in
    -y|--yes)
      ASSUME_YES=true
      ;;
    -f|--compose-file)
      shift
      [ "${1:-}" != "" ] || die "missing value for --compose-file"
      COMPOSE_FILE_PATH="$1"
      ;;
    --stop-services)
      shift
      [ "${1:-}" != "" ] || die "missing value for --stop-services"
      RESTORE_STOP_SERVICES="$1"
      ;;
    --no-pre-backup)
      DO_PRE_BACKUP=false
      ;;
    --skip-checksum)
      VERIFY_CHECKSUM=false
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    -*)
      die "unknown option: $1"
      ;;
    *)
      if [ -n "$SOURCE_FILE" ]; then
        die "only one backup file is allowed"
      fi
      SOURCE_FILE="$1"
      ;;
  esac
  shift
done

[ -n "$SOURCE_FILE" ] || {
  usage
  die "missing backup file"
}

if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  COMPOSE=(docker compose)
fi

choose_compose_file() {
  local defaults=(
    "docker/docker-compose.yml"
    "docker/docker-compose-local.yml"
    "docker/docker-compose.ssl.yml"
    "docker/docker-compose-dev.yml"
    "docker/docker-compose-dev.ssl.yml"
  )
  local available=()
  local idx=1
  local selection=""

  for f in "${defaults[@]}"; do
    if [ -f "$f" ]; then
      available+=("$f")
    fi
  done

  [ "${#available[@]}" -gt 0 ] || die "no compose files found in default locations"

  if [ "${#available[@]}" -eq 1 ] || [ ! -t 0 ] || $ASSUME_YES; then
    COMPOSE_FILE_PATH="${available[0]}"
    return
  fi

  echo "Select compose file:"
  for f in "${available[@]}"; do
    echo "  $idx) $f"
    idx=$((idx + 1))
  done

  echo -n "Choice [1-${#available[@]}] (default 1): "
  read -r selection
  if [ -z "$selection" ]; then
    selection=1
  fi
  [[ "$selection" =~ ^[0-9]+$ ]] || die "invalid compose selection: $selection"
  [ "$selection" -ge 1 ] && [ "$selection" -le "${#available[@]}" ] || die "compose selection out of range: $selection"

  COMPOSE_FILE_PATH="${available[$((selection - 1))]}"
}

if [ -z "$COMPOSE_FILE_PATH" ]; then
  choose_compose_file
fi

compose() {
  "${COMPOSE[@]}" -f "$COMPOSE_FILE_PATH" "$@"
}

require_command gzip
require_command sha256sum
require_command awk
require_command sed
require_command tar

[ -f "$COMPOSE_FILE_PATH" ] || die "compose file not found: $COMPOSE_FILE_PATH"
[ -f "$SOURCE_FILE" ] || die "backup file not found: $SOURCE_FILE"
if [ -n "$RESTORE_STOP_SERVICES" ]; then
  warn "--stop-services/RESTORE_STOP_SERVICES is deprecated and ignored; full stack restart is always used"
fi

if [[ "$SOURCE_FILE" == *.sql.gz ]] && $VERIFY_CHECKSUM; then
  CHECKSUM_FILE="${SOURCE_FILE}.sha256"
  [ -f "$CHECKSUM_FILE" ] || die "checksum file not found: $CHECKSUM_FILE"

  expected_hash="$(awk 'NR==1 {print $1}' "$CHECKSUM_FILE")"
  [ -n "$expected_hash" ] || die "checksum file is empty or invalid: $CHECKSUM_FILE"

  actual_hash="$(sha256sum "$SOURCE_FILE" | awk '{print $1}')"
  if [ "$expected_hash" != "$actual_hash" ]; then
    die "checksum mismatch for $SOURCE_FILE"
  fi
  log "checksum verified for $SOURCE_FILE"
fi

if ! $ASSUME_YES; then
  echo "Compose file: $COMPOSE_FILE_PATH"
  echo "Restore will import '$SOURCE_FILE' into service 'db' and perform a full compose restart (down/up)."
  echo -n "Type 'restore' to continue: "
  read -r answer
  [ "$answer" = "restore" ] || {
    log "aborted"
    exit 0
  }
fi

cleanup() {
  local exit_code="$1"
  if [ "$exit_code" -ne 0 ]; then
    warn "restore failed or interrupted; bringing stack up with compose file: $COMPOSE_FILE_PATH"
    compose up -d || true
  fi
  exit "$exit_code"
}
trap 'cleanup $?' EXIT

if $DO_PRE_BACKUP; then
  [ -f "scripts/database_backup.sh" ] || die "pre-backup script not found: scripts/database_backup.sh"
  log "creating pre-restore safety backup"
  COMPOSE_FILE_PATH="$COMPOSE_FILE_PATH" scripts/database_backup.sh
fi

log "stopping complete compose stack"
compose down

log "starting database service for restore"
compose up -d db

log "waiting for database readiness"
max_tries=60
tries=0
until compose exec -T db sh -c 'MYSQL_PWD="$(cat /run/secrets/db_root_password)" mysqladmin ping -h localhost -uroot --silent' >/dev/null 2>&1; do
  tries=$((tries + 1))
  if [ "$tries" -ge "$max_tries" ]; then
    die "database did not become ready in time"
  fi
  sleep 2
done

log "restoring database from $SOURCE_FILE"
if [[ "$SOURCE_FILE" == *.sql.gz ]]; then
  gzip -dc "$SOURCE_FILE" | compose exec -T db sh -c \
    'MYSQL_PWD="$(cat /run/secrets/db_root_password)" mysql --binary-mode=1 -uroot "$MYSQL_DATABASE"'
elif [[ "$SOURCE_FILE" == *.tgz ]]; then
  sql_file_in_archive="$(tar -tzf "$SOURCE_FILE" | grep -E '\.sql$' | head -n 1 || true)"
  [ -n "$sql_file_in_archive" ] || die "no .sql file found in archive: $SOURCE_FILE"
  tar -xOzf "$SOURCE_FILE" "$sql_file_in_archive" | compose exec -T db sh -c \
    'MYSQL_PWD="$(cat /run/secrets/db_root_password)" mysql --binary-mode=1 -uroot "$MYSQL_DATABASE"'
else
  [[ "$SOURCE_FILE" == *.sql ]] || die "unsupported backup format: $SOURCE_FILE (expected .sql, .sql.gz or .tgz)"
  cat "$SOURCE_FILE" | compose exec -T db sh -c \
    'MYSQL_PWD="$(cat /run/secrets/db_root_password)" mysql --binary-mode=1 -uroot "$MYSQL_DATABASE"'
fi

log "starting complete compose stack"
compose up -d

log "restore finished successfully"
trap - EXIT
