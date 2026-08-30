#!/usr/bin/env bash
# Startet einen zusätzlichen sofortigen Backup-Lauf im bereits laufenden
# Backup-Container. Die normale zeitgesteuerte Sicherung bleibt davon unberührt.

set -Eeuo pipefail

# Unterstützt sowohl das ältere docker-compose als auch Docker Compose v2.
if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  COMPOSE=(docker compose)
fi

# Die Compose-Datei kann für lokale oder SSL-Installationen überschrieben werden.
COMPOSE_FILE_PATH="${COMPOSE_FILE_PATH:-docker/docker-compose.yml}"

[ -f "$COMPOSE_FILE_PATH" ] || { echo "Compose file not found: $COMPOSE_FILE_PATH" >&2; exit 2; }

# Ersetzt diesen Prozess durch den eigentlichen Einmal-Backup-Job im Container.
exec "${COMPOSE[@]}" -f "$COMPOSE_FILE_PATH" exec -T backup /usr/local/bin/openfls-backup-job
