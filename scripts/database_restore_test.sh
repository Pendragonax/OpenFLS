#!/usr/bin/env bash
# Testet einen Dump in einer vollständig getrennten, kurzlebigen MySQL-Instanz.

set -Eeuo pipefail
umask 077

# Wählt den auf dem System verfügbaren Compose-Befehl.
if command -v docker-compose >/dev/null 2>&1; then COMPOSE=(docker-compose); else COMPOSE=(docker compose); fi

# Konfiguriert Ablage, eigene Test-Compose-Datei und isolierten Projektnamen.
BACKUP_DIR="${BACKUP_DIR:-$PWD/docker/backup}"
COMPOSE_FILE="${RESTORE_TEST_COMPOSE_FILE:-docker/docker-compose-restore-test.yml}"
PROJECT_NAME="${RESTORE_TEST_PROJECT_NAME:-openfls-restore-test}"
SOURCE_FILE="${1:-}"

# Ohne Argument wird ausschließlich die zeitlich neueste komprimierte Sicherung gewählt.
if [ -z "$SOURCE_FILE" ]; then
  SOURCE_FILE="$(find "$BACKUP_DIR" -maxdepth 1 -type f -name '*.sql.gz' -printf '%T@ %p\n' | sort -nr | head -n 1 | cut -d' ' -f2-)"
fi
[ -n "$SOURCE_FILE" ] && [ -f "$SOURCE_FILE" ] || { echo 'No .sql.gz backup file found' >&2; exit 2; }
[ -f "$COMPOSE_FILE" ] || { echo "Restore-test Compose file not found: $COMPOSE_FILE" >&2; exit 2; }

# Prüft vor dem Import die Integrität der Sicherungsdatei.
CHECKSUM_FILE="${SOURCE_FILE}.sha256"
[ -f "$CHECKSUM_FILE" ] || { echo "Checksum file not found: $CHECKSUM_FILE" >&2; exit 2; }
(cd "$(dirname "$SOURCE_FILE")" && sha256sum -c "$(basename "$CHECKSUM_FILE")")

# Hält das Testergebnis separat vom Produktivstatus für eine spätere UI fest.
STATUS_DIR="$BACKUP_DIR/status"
mkdir -p "$STATUS_DIR"
started_epoch="$(date +%s)"
write_status() {
  local outcome="$1" message="$2"
  printf '{"timestamp":"%s","level":"%s","event_name":"restore_test.completed","outcome":"%s","service":"openfls-restore-test","message":"%s","backup_file":"%s","duration_seconds":%s}\n' \
    "$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')" "$( [ "$outcome" = success ] && echo INFO || echo ERROR )" "$outcome" "$message" "$(basename "$SOURCE_FILE")" "$(( $(date +%s) - started_epoch ))" > "$STATUS_DIR/restore-test-latest.json"
  cat "$STATUS_DIR/restore-test-latest.json" >> "$STATUS_DIR/restore-test-history.jsonl"
}
# Entfernt unabhängig vom Ergebnis nur Container, Netzwerk und Volume des Testprojekts.
cleanup() { "${COMPOSE[@]}" -p "$PROJECT_NAME" -f "$COMPOSE_FILE" down -v --remove-orphans >/dev/null 2>&1 || true; }
on_error() { write_status failure 'Isolated restore test failed' || true; exit "$1"; }
trap cleanup EXIT
trap 'on_error $?' ERR

# Startet mit einer leeren Testumgebung und niemals mit dem Produktiv-Volume.
"${COMPOSE[@]}" -p "$PROJECT_NAME" -f "$COMPOSE_FILE" down -v --remove-orphans >/dev/null 2>&1 || true
"${COMPOSE[@]}" -p "$PROJECT_NAME" -f "$COMPOSE_FILE" up -d restore-test-db
# Wartet nicht nur auf MySQL, sondern auf die vollständig angelegte Testdatenbank.
tries=0
until "${COMPOSE[@]}" -p "$PROJECT_NAME" -f "$COMPOSE_FILE" exec -T restore-test-db sh -c "mysqladmin ping -h localhost -uroot --silent >/dev/null && mysql -N -uroot -e \"SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'openfls_restore_test';\" | grep -qx openfls_restore_test" >/dev/null 2>&1; do
  tries=$((tries + 1))
  if [ "$tries" -ge 60 ]; then
    write_status failure 'Temporary restore-test database did not become ready'
    exit 1
  fi
  sleep 2
done
# Importiert den Dump und prüft danach Tabellenbestand sowie Flyway-Historie.
gzip -dc "$SOURCE_FILE" | "${COMPOSE[@]}" -p "$PROJECT_NAME" -f "$COMPOSE_FILE" exec -T restore-test-db mysql -uroot openfls_restore_test
"${COMPOSE[@]}" -p "$PROJECT_NAME" -f "$COMPOSE_FILE" exec -T restore-test-db mysql -N -uroot openfls_restore_test -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'openfls_restore_test';" | awk '$1 > 0 { found = 1 } END { exit !found }'
"${COMPOSE[@]}" -p "$PROJECT_NAME" -f "$COMPOSE_FILE" exec -T restore-test-db mysql -N -uroot openfls_restore_test -e "SHOW TABLES LIKE 'flyway_schema_history';" | grep -qx flyway_schema_history
write_status success 'Backup was restored and the OpenFLS schema was validated'
echo 'Restore test completed successfully.'
