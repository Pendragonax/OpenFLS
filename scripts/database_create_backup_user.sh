#!/usr/bin/env bash
# Richtet einmalig das eingeschränkte MySQL-Konto für den Backup-Container ein.

set -Eeuo pipefail

# Unterstützt beide verbreiteten Compose-Aufrufe.
if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  COMPOSE=(docker compose)
fi

# Diese Werte bestimmen Ziel-Datenbank, Konto und Passwortdatei.
COMPOSE_FILE_PATH="${COMPOSE_FILE_PATH:-docker/docker-compose.yml}"
MYSQL_DATABASE="${MYSQL_DATABASE:-openfls}"
MYSQL_BACKUP_USER="${MYSQL_BACKUP_USER:-openfls_backup}"
MYSQL_BACKUP_PASSWORD_FILE="${MYSQL_BACKUP_PASSWORD_FILE:-secrets/db_backup_password.secret}"

# Akzeptiert nur sichere Bezeichner, weil sie später in SQL verwendet werden.
case "$MYSQL_DATABASE" in *[!A-Za-z0-9_]*|'') echo 'Invalid MYSQL_DATABASE' >&2; exit 2;; esac
case "$MYSQL_BACKUP_USER" in *[!A-Za-z0-9_]*|'') echo 'Invalid MYSQL_BACKUP_USER' >&2; exit 2;; esac
[ -f "$COMPOSE_FILE_PATH" ] || { echo "Compose file not found: $COMPOSE_FILE_PATH" >&2; exit 2; }
[ -r "$MYSQL_BACKUP_PASSWORD_FILE" ] || { echo "Backup password file is not readable: $MYSQL_BACKUP_PASSWORD_FILE" >&2; exit 2; }

# Liest das Secret, ohne es auszugeben, und maskiert Hochkommas für SQL.
backup_password="$(<"$MYSQL_BACKUP_PASSWORD_FILE")"
[ -n "$backup_password" ] || { echo 'Backup password must not be empty' >&2; exit 2; }
escaped_password="${backup_password//\'/\'\'}"

# Nutzt den vorhandenen Root-Zugang nur für die Anlage und Rechtevergabe.
"${COMPOSE[@]}" -f "$COMPOSE_FILE_PATH" exec -T db sh -c \
  "MYSQL_PWD=\"\$(cat /run/secrets/db_root_password)\" mysql -uroot" <<SQL
CREATE USER IF NOT EXISTS '${MYSQL_BACKUP_USER}'@'%' IDENTIFIED BY '${escaped_password}';
ALTER USER '${MYSQL_BACKUP_USER}'@'%' IDENTIFIED BY '${escaped_password}';
GRANT SELECT, SHOW VIEW, TRIGGER ON \`${MYSQL_DATABASE}\`.* TO '${MYSQL_BACKUP_USER}'@'%';
FLUSH PRIVILEGES;
SQL

echo 'Backup database user has been created or updated.'
