[![GitHub release](https://img.shields.io/badge/version-3.1.0-blue)](https://GitHub.com/Pendragonax/OpenFLS/releases/)
[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-ready-2496ED?logo=docker&logoColor=white)

# OpenFLS

> Client-related documentation, time tracking and evaluation for social institutions — an open-source alternative to proprietary case-management software.

📖 Read this in another language: 🇬🇧 [English](https://github.com/Pendragonax/OpenFLS/blob/main/README.md) · 🇩🇪 [Deutsch](https://github.com/Pendragonax/OpenFLS/blob/main/README.de.md)

OpenFLS is a multi-container application in which staff document the hours worked
with clients on the basis of assistance plans: times, goals, contents, progress
and further billing-relevant data. The recorded data is the basis for
comprehensible case documentation, cost accounting and evidence towards
cost carriers, utilization and resource evaluations, and operational statistics.

It is oriented towards social institutions in the German federal state of Hesse
and was designed against a real institution and the old and new framework
agreements of the state of Hesse. The goal is independence from software vendors
and the self-administration that comes with it.

---

## ✨ Features

- 🕒 **Time tracking** with clear semantics (time zone, start, end, duration, correction status)
- 🎯 **Assistance-plan work** — goals, contents, progress, hour contingents and corridors
- 💶 **Billing-relevant data** and evidence towards cost carriers
- 📊 **Evaluations** for utilization, resource use and operational statistics
- 🗃️ **Archiving** of clients and employees, included in the defined exports
- 🔐 **Role-based access** (Administrator / Lead / User), stateless JWT authentication
- 🧾 **Audit logging** for relevant changes to documentation, plans and billing data
- 🛠️ **Operations built in** — structured logging with a live log view, an automated
  daily database backup with isolated restore-test, and a read-only
  *Datensicherung* dashboard in **Settings**

## 🧱 Tech stack

| Layer     | Stack                                                                       |
| --------- | -------------------------------------------------------------------------- |
| Backend   | Kotlin 2.3 · Spring Boot 3.5 · Java 21 · Spring Data JPA · Flyway · Gradle |
| Frontend  | Angular 21 · TypeScript 5.9 · Angular Material · Bootstrap 5               |
| Database  | MySQL 8.4 (pinned by digest)                                              |
| Infra     | Docker Compose · nginx reverse proxy · Adminer                            |

## 📁 Project structure

```text
.
├── backend/    # Kotlin/Spring Boot — domain logic, persistence, REST API
├── frontend/   # Angular — user interface
├── docker/     # Compose files per scope + docker-compose.env
├── proxy/      # nginx reverse-proxy configuration
├── scripts/    # setup, backup, restore and maintenance scripts
├── secrets/    # local secret files (git-ignored)
└── docs/       # architecture, operations and domain documentation
```

## 🚀 Getting started

### Prerequisites

- A Linux host with **Docker Engine** and **Docker Compose v2**
- **Git**

### 1. Clone and bootstrap

```bash
git clone git@github.com:Pendragonax/OpenFLS.git
cd OpenFLS
scripts/init.sh          # type "go" — creates secrets/ and the JWT signing keys
```

### 2. Configure

| File | What to set |
| --- | --- |
| `secrets/*.secret` | Replace the placeholder database credentials. **Do not leave a trailing newline.** `db_backup_password.secret` is generated randomly. |
| `docker/.env` | `UID` / `GID` of the host user that owns the mounted volumes (default `1000`). |
| `docker/docker-compose.env` | Database name, session timeout, **backup schedule** (`BACKUP_TIME`, `BACKUP_TIMEZONE`, `BACKUP_INTERVAL_DAYS`), local retention. |

The application is served on **port 8000**. To change it, edit the `ports:` mapping
of the `proxy` service in the compose file.

### 3. Start

```bash
# The production compose uses an external data volume — create it once:
docker volume create openfls_open-fls-db

docker compose -f docker/docker-compose.yml up -d
```

Create the restricted backup database user once while the stack is running — see
[`docs/backup-operation.md`](docs/backup-operation.md).

### 4. First login

Open <http://localhost:8000>. If no user exists yet, log in with **`admin` / `admin`**,
then immediately create a real employee with the **Administrator** role under the
*Mitarbeiter* menu — otherwise the database has to be reset. A new employee's
initial password equals the username and can be changed at any time afterwards.

<details>
<summary>Run with locally built images</summary>

```bash
scripts/build_local_images.sh
docker volume create openfls_open-fls-db
docker compose -f docker/docker-compose-local.yml up -d
```
</details>

<details>
<summary>HTTPS / SSL</summary>

Use the `docker/docker-compose*.ssl.yml` variant of the desired scope and provide
the certificate/key files referenced by `proxy/ssl.nginx.conf`.
</details>

## 🛠️ Development

```bash
# Infrastructure + frontend with hot reload (no backend container here)
docker compose -f docker/docker-compose-dev.yml up
```

The backend runs from your IDE or the CLI:

```bash
cp backend/run.env.example backend/run.env      # then adjust if needed
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The working directory must be `backend/`; `run.env` is loaded via
`spring.config.import` (see the comments in `run.env.example`).

```bash
# Tests
cd backend  && ./gradlew test
cd frontend && npm test

# Full backend verification (required after backend changes)
cd backend && ./gradlew clean build
```

## 🐳 Compose files

| File | Purpose |
| --- | --- |
| `docker/docker-compose.yml` | Production at the customer (published `ghcr.io` images) |
| `docker/docker-compose-local.yml` | Production-like, using locally built images |
| `docker/docker-compose-dev.yml` | Development — frontend hot reload, backend from the IDE |
| `docker/docker-compose*.ssl.yml` | The matching scope with HTTPS |
| `docker/docker-compose-restore-test.yml` | Isolated database restore test |

## 🗄️ Backups

The `backup` service creates a logical MySQL dump at a configurable time of day
(`BACKUP_TIME` / `BACKUP_TIMEZONE`), every `BACKUP_INTERVAL_DAYS` days. It keeps a
local retention window, verifies checksums, and writes status files that the
in-app **Settings → Datensicherung** view reads (status, history, next run,
actionable hints on failure). Restore and restore-test run via
`scripts/database_restore.sh` / `scripts/database_restore_test.sh`.

- Operation & configuration → [`docs/backup-operation.md`](docs/backup-operation.md)
- Migrating an existing installation → [`docs/backup-migration.md`](docs/backup-migration.md)
- Restoring a pre-3.1 backup (`.tgz` / `.sql.gz` / `.sql`) → *"Wiederherstellung einer alten Sicherung"* in `docs/backup-operation.md`

## 🔧 Scripts

| Script | Purpose |
| --- | --- |
| `scripts/init.sh` | One-time bootstrap: default secrets + JWT keys |
| `scripts/build_local_images.sh` | Build the `:local` backend/frontend images |
| `scripts/database_create_backup_user.sh` | Create/refresh the restricted backup database user |
| `scripts/database_backup.sh` | Trigger one immediate backup in the running container |
| `scripts/database_restore.sh` | Restore a dump into the production database (`.sql.gz` / `.sql` / `.tgz`) |
| `scripts/database_restore_test.sh` | Import a dump into a throwaway MySQL instance and validate it |
| `scripts/database_remove_db_volume.sh` | Delete the local database container and volume (asks for confirmation) |

## 📚 Documentation

- [`docs/backup-operation.md`](docs/backup-operation.md) · [`docs/backup-migration.md`](docs/backup-migration.md) — backups
- [`docs/logging-guide.md`](docs/logging-guide.md) — logging rules (mandatory fields, levels, audit/security separation, retention)
- [`AGENTS.md`](AGENTS.md) — binding project and contribution rules
- [`CHANGELOG.md`](CHANGELOG.md) — release notes

## 🤝 Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) and the binding rules in
[`AGENTS.md`](AGENTS.md). Business rules and validation live in the backend; the
frontend is Angular (React only on an explicitly commissioned migration).

## 📄 License

Licensed under the **GPLv3** — see [`LICENSE`](LICENSE).
