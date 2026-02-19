# MySQL Backup and Restore

## Storage Path
Automatic backups are written to `./backup` relative to the directory where `docker compose` / `docker-compose` is executed.

Compose mount:

- `${PWD}/backup:/backup`

## Secrets
Database credentials are injected via secret files and read from `/run/secrets`:

- `db_root_password`
- `db_password`
- `db_user`

The backup/restore scripts use `db_root_password` and restore into `$MYSQL_DATABASE`.

## Automatic Backups
The compose files include a `backup` service (`databack/mysql-backup:latest`) that runs `dump` continuously.

Defaults from `docker/docker-compose.env`:

- `BACKUP_INTERVAL_MINUTES=1440` (once per day)
- `BACKUP_RETENTION_DAYS=30`

## Manual Backup (On Demand)
Run:

```bash
scripts/database_backup.sh
```

Environment variables:

- `COMPOSE_FILE_PATH` (default: `docker/docker-compose.yml`)
- `BACKUP_DIR` (default: `${PWD}/backup`)

Output:

- Backup file: `<timestamp>-openfls.sql.gz`
- Checksum file: `<timestamp>-openfls.sql.gz.sha256`

## Manual Restore (On Demand)
Run:

```bash
scripts/database_restore.sh [options] <backup.sql.gz|backup.sql|backup.tgz>
```

Supported input formats:

- `.sql.gz`
- `.sql`
- `.tgz` (must contain at least one `.sql` file)

### Restore Options

- `-y`, `--yes`: Skip interactive confirmation.
- `-f`, `--compose-file <path>`: Compose file path (overrides `COMPOSE_FILE_PATH`).
- `--stop-services "svc1 svc2"`: Deprecated and ignored (full stack restart is always used).
- `--no-pre-backup`: Skip the safety backup that runs before restore.
- `--skip-checksum`: Skip SHA256 verification for `.sql.gz` backups.
- `-h`, `--help`: Show usage help.

### Restore Environment Variables

- `COMPOSE_FILE_PATH`: Compose file path (optional).
- `RESTORE_STOP_SERVICES`: Deprecated and ignored (full stack restart is always used).

### Restore Behavior

- Uses `docker-compose` if available, otherwise `docker compose`.
- If `COMPOSE_FILE_PATH` is not set, it auto-selects or interactively asks from:
  - `docker/docker-compose.yml`
  - `docker/docker-compose-local.yml`
  - `docker/docker-compose.ssl.yml`
  - `docker/docker-compose-dev.yml`
  - `docker/docker-compose-dev.ssl.yml`
- Requires backup file argument and existing compose file.
- For `.sql.gz`, checksum validation is enabled by default and expects `<backup>.sha256`.
- Unless `-y` is used, you must type `restore` to continue.
- Creates a safety backup before restore via `scripts/database_backup.sh` unless `--no-pre-backup` is set.
- Runs `docker compose down` for the full stack.
- Starts `db` only, waits for MySQL readiness, then imports the dump.
- Starts the full stack again via `docker compose up -d`.

### Restore Examples

Interactive restore with checksum verification:

```bash
scripts/database_restore.sh ./backup/20260219_120000-openfls.sql.gz
```

Non-interactive restore with explicit compose file:

```bash
scripts/database_restore.sh -y -f docker/docker-compose-dev.yml ./backup/20260219_120000-openfls.sql.gz
```

Restore without checksum verification:

```bash
scripts/database_restore.sh --skip-checksum -f docker/docker-compose-dev.yml ./backup/backup.sql.gz
```

Restore without pre-backup:

```bash
scripts/database_restore.sh --no-pre-backup -f docker/docker-compose-dev.yml ./backup/backup.sql
```

Restore from `.tgz` (the `--stop-services` option is accepted but ignored):

```bash
scripts/database_restore.sh --stop-services "backend frontend proxy" -f docker/docker-compose-dev.yml ./backup/backup.tgz
```

Alternative restore via `backup` container:

```bash
docker compose -f docker/docker-compose.yml run --rm \
  -e DB_RESTORE_TARGET=/backup/<file>.sql.gz \
  backup restore
```

## Notes
- Restore is always manual by design.
- Test restore regularly in a non-production environment.
- Run scripts from the repository root so relative paths (`scripts/...`, `docker/...`) resolve correctly.
