#!/usr/bin/env bash
# Legt lokale Standard-Secret-Dateien und die Backup-Verzeichnisse für die
# Entwicklung an.
set -euo pipefail

# Ermittelt den Projektstamm unabhängig vom aktuellen Arbeitsverzeichnis.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIR="${ROOT_DIR}/secrets"
BACKUP_STATUS_DIR="${ROOT_DIR}/docker/backup/status"

# Erstellt die Zielordner nur, wenn sie noch nicht vorhanden sind.
mkdir -p "$DIR"

# Die Backup-Ablage muss vor dem ersten "docker compose up" existieren und dem
# ausführenden Benutzer gehören, sonst legt Docker sie als root an und der
# Backup-Container (UID 1000) kann darin weder schreiben noch sperren.
mkdir -p "$BACKUP_STATUS_DIR"
chmod 700 "${ROOT_DIR}/docker/backup" "$BACKUP_STATUS_DIR"

# Schreibt bewusst unsichere Platzhalter, die vor einem Produktivbetrieb ersetzt
# werden müssen. Nur das Backup-Passwort wird zufällig erzeugt, weil ein echtes
# Datenbankkonto daran hängt.
echo -e "\e[36m[INFO] create default secrets at ./secrets\e[0m"
[ -f "$DIR/db_password.secret" ] || echo -n "user_password" > "$DIR/db_password.secret"
[ -f "$DIR/db_root_password.secret" ] || echo -n "password" > "$DIR/db_root_password.secret"
[ -f "$DIR/db_user.secret" ] || echo -n "user" > "$DIR/db_user.secret"
if [ ! -f "$DIR/db_backup_password.secret" ]; then
  openssl rand -base64 24 | tr -d '\n' > "$DIR/db_backup_password.secret"
fi
chmod 600 "$DIR"/*.secret

echo -e "\e[36m[INFO] make sure to change the content of the secret files. Dont use the default values in production!\e[0m"
echo -e "\e[36m[INFO] secrets created.\e[0m"
