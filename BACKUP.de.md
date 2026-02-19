# MySQL Backup und Restore

## Speicherort
Automatische Backups werden nach `./backup` geschrieben, relativ zu dem Verzeichnis, in dem `docker compose` / `docker-compose` ausgefuehrt wird.

Compose-Mount:

- `${PWD}/backup:/backup`

## Secrets
Datenbank-Zugangsdaten werden ueber Secret-Dateien injiziert und aus `/run/secrets` gelesen:

- `db_root_password`
- `db_password`
- `db_user`

Die Backup-/Restore-Skripte verwenden `db_root_password` und stellen in `$MYSQL_DATABASE` wieder her.

## Automatische Backups
Die Compose-Dateien enthalten einen `backup`-Service (`databack/mysql-backup:latest`), der `dump` kontinuierlich ausfuehrt.

Standardwerte aus `docker/docker-compose.env`:

- `BACKUP_INTERVAL_MINUTES=1440` (einmal pro Tag)
- `BACKUP_RETENTION_DAYS=30`

## Manuelles Backup (On Demand)
Aufruf:

```bash
scripts/database_backup.sh
```

Umgebungsvariablen:

- `COMPOSE_FILE_PATH` (Standard: `docker/docker-compose.yml`)
- `BACKUP_DIR` (Standard: `${PWD}/backup`)

Ausgabe:

- Backup-Datei: `<timestamp>-openfls.sql.gz`
- Checksum-Datei: `<timestamp>-openfls.sql.gz.sha256`

## Manuelles Restore (On Demand)
Aufruf:

```bash
scripts/database_restore.sh [options] <backup.sql.gz|backup.sql|backup.tgz>
```

Unterstuetzte Eingabeformate:

- `.sql.gz`
- `.sql`
- `.tgz` (muss mindestens eine `.sql`-Datei enthalten)

### Restore-Optionen

- `-y`, `--yes`: Interaktive Bestaetigung ueberspringen.
- `-f`, `--compose-file <path>`: Pfad zur Compose-Datei (ueberschreibt `COMPOSE_FILE_PATH`).
- `--stop-services "svc1 svc2"`: Veraltet und ohne Wirkung (vollstaendiger Stack-Neustart wird immer verwendet).
- `--no-pre-backup`: Sicherheits-Backup vor dem Restore ueberspringen.
- `--skip-checksum`: SHA256-Pruefung fuer `.sql.gz`-Backups ueberspringen.
- `-h`, `--help`: Hilfe anzeigen.

### Restore-Umgebungsvariablen

- `COMPOSE_FILE_PATH`: Pfad zur Compose-Datei (optional).
- `RESTORE_STOP_SERVICES`: Veraltet und ohne Wirkung (vollstaendiger Stack-Neustart wird immer verwendet).

### Restore-Verhalten

- Nutzt `docker-compose`, falls verfuegbar, sonst `docker compose`.
- Wenn `COMPOSE_FILE_PATH` nicht gesetzt ist, wird automatisch ausgewaehlt oder interaktiv gefragt aus:
  - `docker/docker-compose.yml`
  - `docker/docker-compose-local.yml`
  - `docker/docker-compose.ssl.yml`
  - `docker/docker-compose-dev.yml`
  - `docker/docker-compose-dev.ssl.yml`
- Benoetigt ein Backup-Dateiargument und eine vorhandene Compose-Datei.
- Fuer `.sql.gz` ist die Checksum-Pruefung standardmaessig aktiv und erwartet `<backup>.sha256`.
- Ohne `-y` musst du zur Bestaetigung `restore` eingeben.
- Erstellt vor dem Restore ein Sicherheits-Backup ueber `scripts/database_backup.sh`, sofern nicht `--no-pre-backup` gesetzt ist.
- Fuehrt fuer den kompletten Stack `docker compose down` aus.
- Startet nur `db`, wartet auf MySQL-Readiness und importiert dann den Dump.
- Startet danach den kompletten Stack wieder mit `docker compose up -d`.

### Restore-Beispiele

Interaktives Restore mit Checksum-Pruefung:

```bash
scripts/database_restore.sh ./backup/20260219_120000-openfls.sql.gz
```

Nicht-interaktives Restore mit expliziter Compose-Datei:

```bash
scripts/database_restore.sh -y -f docker/docker-compose-dev.yml ./backup/20260219_120000-openfls.sql.gz
```

Restore ohne Checksum-Pruefung:

```bash
scripts/database_restore.sh --skip-checksum -f docker/docker-compose-dev.yml ./backup/backup.sql.gz
```

Restore ohne Pre-Backup:

```bash
scripts/database_restore.sh --no-pre-backup -f docker/docker-compose-dev.yml ./backup/backup.sql
```

Restore aus `.tgz` (`--stop-services` wird akzeptiert, aber ignoriert):

```bash
scripts/database_restore.sh --stop-services "backend frontend proxy" -f docker/docker-compose-dev.yml ./backup/backup.tgz
```

Alternative Restore-Variante ueber den `backup`-Container:

```bash
docker compose -f docker/docker-compose.yml run --rm \
  -e DB_RESTORE_TARGET=/backup/<file>.sql.gz \
  backup restore
```

## Hinweise
- Restore ist bewusst nur manuell.
- Restore regelmaessig in einer Nicht-Produktivumgebung testen.
- Skripte aus dem Repository-Root ausfuehren, damit relative Pfade (`scripts/...`, `docker/...`) korrekt aufgeloest werden.
