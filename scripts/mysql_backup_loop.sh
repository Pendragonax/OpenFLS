#!/bin/sh

set -eu

DB_HOST="${MYSQL_HOST:-open_fls_db}"
DB_NAME="${MYSQL_DATABASE:-openfls}"
ROOT_PASSWORD_FILE="${MYSQL_ROOT_PASSWORD_FILE:-/run/secrets/db_root_password}"
BACKUP_DIR="${BACKUP_DIR:-/backup}"
BACKUP_INTERVAL_SECONDS="${BACKUP_INTERVAL_SECONDS:-86400}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"

timestamp() {
  date +"%Y-%m-%d %H:%M:%S"
}

log() {
  echo "[$(timestamp)] [backup] $1"
}

require_password_file() {
  if [ ! -f "$ROOT_PASSWORD_FILE" ]; then
    log "root password file not found at $ROOT_PASSWORD_FILE"
    exit 1
  fi
}

wait_for_database() {
  ROOT_PASSWORD="$(cat "$ROOT_PASSWORD_FILE")"
  export MYSQL_PWD="$ROOT_PASSWORD"

  until mysqladmin ping -h "$DB_HOST" -uroot --silent >/dev/null 2>&1; do
    log "waiting for mysql on $DB_HOST ..."
    sleep 5
  done

  log "mysql is reachable"
}

create_backup() {
  ROOT_PASSWORD="$(cat "$ROOT_PASSWORD_FILE")"
  export MYSQL_PWD="$ROOT_PASSWORD"

  RUN_TS="$(date +"%Y%m%d_%H%M%S")"
  BASENAME="$RUN_TS-$DB_NAME.sql"
  TARGET_SQL="$BACKUP_DIR/$BASENAME"
  TARGET_GZ="$TARGET_SQL.gz"

  log "creating backup $TARGET_GZ"
  mysqldump \
    -h "$DB_HOST" \
    -uroot \
    --single-transaction \
    --routines \
    --events \
    --triggers \
    "$DB_NAME" > "$TARGET_SQL"

  gzip "$TARGET_SQL"
  sha256sum "$TARGET_GZ" > "$TARGET_GZ.sha256"
  log "backup finished: $TARGET_GZ"
}

cleanup_old_backups() {
  find "$BACKUP_DIR" -maxdepth 1 -type f -name "*.sql.gz" -mtime "+$BACKUP_RETENTION_DAYS" -delete
  find "$BACKUP_DIR" -maxdepth 1 -type f -name "*.sql.gz.sha256" -mtime "+$BACKUP_RETENTION_DAYS" -delete
}

main() {
  mkdir -p "$BACKUP_DIR"
  require_password_file
  wait_for_database

  while true; do
    create_backup
    cleanup_old_backups
    sleep "$BACKUP_INTERVAL_SECONDS"
  done
}

main "$@"
