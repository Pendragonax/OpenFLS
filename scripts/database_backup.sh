#!/usr/bin/env bash

set -euo pipefail

if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  COMPOSE=(docker compose)
fi

COMPOSE_FILE_PATH="${COMPOSE_FILE_PATH:-docker/docker-compose.yml}"
BACKUP_DIR="${BACKUP_DIR:-$PWD/backup}"

if [ ! -f "$COMPOSE_FILE_PATH" ]; then
  echo "[ERROR] compose file not found: $COMPOSE_FILE_PATH" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"

RUN_TS="$(date +"%Y%m%d_%H%M%S")"
TARGET_GZ="$BACKUP_DIR/$RUN_TS-openfls.sql.gz"

echo "[INFO] creating backup at $TARGET_GZ"
"${COMPOSE[@]}" -f "$COMPOSE_FILE_PATH" exec -T db sh -c \
  'MYSQL_PWD="$(cat /run/secrets/db_root_password)" mysqldump -uroot --single-transaction --routines --events --triggers "$MYSQL_DATABASE"' \
  | gzip > "$TARGET_GZ"

sha256sum "$TARGET_GZ" > "$TARGET_GZ.sha256"
echo "[INFO] backup finished"
