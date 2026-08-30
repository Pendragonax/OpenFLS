#!/usr/bin/env bash

set -Eeuo pipefail

if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  COMPOSE=(docker compose)
fi

COMPOSE_FILE_PATH="${COMPOSE_FILE_PATH:-docker/docker-compose.yml}"

[ -f "$COMPOSE_FILE_PATH" ] || { echo "Compose file not found: $COMPOSE_FILE_PATH" >&2; exit 2; }

exec "${COMPOSE[@]}" -f "$COMPOSE_FILE_PATH" exec -T backup /usr/local/bin/openfls-backup-job
