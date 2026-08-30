[![GitHub release](https://img.shields.io/badge/version-3.1.0-blue)](https://GitHub.com/Pendragonax/OpenFLS/releases/)
[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](http://perso.crans.org/besson/LICENSE.html)

![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-ready-2496ED?logo=docker&logoColor=white)

# OpenFLS

> Klientenbezogene Dokumentation, Zeiterfassung und Auswertung für soziale Einrichtungen — eine Open-Source-Alternative zu proprietärer Falldokumentations-Software.

📖 In einer anderen Sprache lesen: 🇬🇧 [English](https://github.com/Pendragonax/OpenFLS/blob/main/README.md) · 🇩🇪 [Deutsch](https://github.com/Pendragonax/OpenFLS/blob/main/README.de.md)

OpenFLS ist eine Multi-Container-Anwendung, in der Mitarbeitende die mit
Klient:innen geleisteten Stunden auf Grundlage von Hilfeplänen dokumentieren:
Zeiten, Ziele, Inhalte, Fortschritte und weitere abrechnungsrelevante Angaben.
Die erfassten Daten sind die Grundlage für nachvollziehbare Falldokumentation
und Hilfeplanarbeit, Kostenabrechnung und Nachweise gegenüber Kostenträgern,
Auswertungen zu Auslastung und Ressourceneinsatz sowie fachliche und
betriebliche Statistiken.

Die Anwendung orientiert sich an sozialen Einrichtungen im Bundesland Hessen und
wurde anhand einer realen Einrichtung sowie des alten und neuen Rahmenvertrags
des Landes Hessen entworfen. Ziel ist die Unabhängigkeit von Software-Unternehmen
und die damit einhergehende Selbstverwaltung.

---

## ✨ Funktionen

- 🕒 **Zeiterfassung** mit klarer Semantik (Zeitzone, Beginn, Ende, Dauer, Korrekturstatus)
- 🎯 **Hilfeplanarbeit** — Ziele, Inhalte, Fortschritte, Stundenkontingente und -korridore
- 💶 **Abrechnungsrelevante Daten** und Nachweise gegenüber Kostenträgern
- 📊 **Auswertungen** zu Auslastung, Ressourceneinsatz und betrieblichen Statistiken
- 🗃️ **Archivierung** von Klient:innen und Mitarbeitenden, enthalten in den vorgesehenen Exporten
- 🔐 **Rollenbasierte Berechtigungen** (Administrator / Leitung / Nutzer), zustandslose JWT-Authentifizierung
- 🧾 **Audit-Log** für relevante Änderungen an Dokumentation, Hilfeplänen und Abrechnungsdaten
- 🛠️ **Betrieb ist eingebaut** — strukturiertes Logging mit Live-Ansicht, ein
  automatisches tägliches Datenbank-Backup mit isoliertem Restore-Test sowie eine
  Nur-Lese-Übersicht *Datensicherung* unter **Einstellungen**

## 🧱 Technologie

| Ebene     | Stack                                                                       |
| --------- | ------------------------------------------------------------------------- |
| Backend   | Kotlin 2.3 · Spring Boot 3.5 · Java 21 · Spring Data JPA · Flyway · Gradle |
| Frontend  | Angular 21 · TypeScript 5.9 · Angular Material · Bootstrap 5              |
| Datenbank | MySQL 8.4 (per Digest gepinnt)                                           |
| Infra     | Docker Compose · nginx Reverse-Proxy · Adminer                          |

## 📁 Projektstruktur

```text
.
├── backend/    # Kotlin/Spring Boot — Fachlogik, Persistenz, REST-API
├── frontend/   # Angular — Benutzeroberfläche
├── docker/     # Compose-Dateien je Scope + docker-compose.env
├── proxy/      # nginx-Reverse-Proxy-Konfiguration
├── scripts/    # Setup-, Backup-, Restore- und Wartungsskripte
├── secrets/    # lokale Secret-Dateien (nicht versioniert)
└── docs/       # Architektur-, Betriebs- und Fach-Dokumentation
```

## 🚀 Erste Schritte

### Voraussetzungen

- Ein Linux-Host mit **Docker Engine** und **Docker Compose v2**
- **Git**

### 1. Klonen und initialisieren

```bash
git clone git@github.com:Pendragonax/OpenFLS.git
cd OpenFLS
scripts/init.sh          # "go" eingeben — legt secrets/ und die JWT-Schlüssel an
```

### 2. Konfigurieren

| Datei | Was einzustellen ist |
| --- | --- |
| `secrets/*.secret` | Die Platzhalter-Zugangsdaten der Datenbank ersetzen. **Keine leere Zeile am Ende lassen.** `db_backup_password.secret` wird zufällig erzeugt. |
| `docker/.env` | `UID` / `GID` des Host-Benutzers, dem die gemounteten Volumes gehören (Standard `1000`). |
| `docker/docker-compose.env` | Datenbankname, Sitzungsdauer, **Backup-Zeitplan** (`BACKUP_TIME`, `BACKUP_TIMEZONE`, `BACKUP_INTERVAL_DAYS`), lokale Aufbewahrung. |

Die Anwendung läuft auf **Port 8000**. Zum Ändern die `ports:`-Zuordnung des
`proxy`-Dienstes in der Compose-Datei anpassen.

### 3. Starten

```bash
# Die Produktions-Compose-Datei nutzt ein externes Datenvolume — einmalig anlegen:
docker volume create openfls_open-fls-db

docker compose -f docker/docker-compose.yml up -d
```

Den eingeschränkten Backup-Datenbankbenutzer einmalig anlegen, während der Stack
läuft — siehe [`docs/backup-operation.md`](docs/backup-operation.md).

### 4. Erste Anmeldung

<http://localhost:8000> öffnen. Ist noch kein Nutzer vorhanden, mit
**`admin` / `admin`** anmelden und danach sofort unter *Mitarbeiter* einen echten
Mitarbeitenden mit der Rolle **Administrator** anlegen — andernfalls muss die
Datenbank zurückgesetzt werden. Das Passwort eines neuen Mitarbeitenden ist
initial gleich dem Benutzernamen und kann jederzeit geändert werden.

<details>
<summary>Mit lokal gebauten Images starten</summary>

```bash
scripts/build_local_images.sh
docker volume create openfls_open-fls-db
docker compose -f docker/docker-compose-local.yml up -d
```
</details>

<details>
<summary>HTTPS / SSL</summary>

Die `docker/docker-compose*.ssl.yml`-Variante des gewünschten Scopes verwenden und
die in `proxy/ssl.nginx.conf` referenzierten Zertifikat-/Schlüsseldateien
bereitstellen.
</details>

## 🛠️ Entwicklung

```bash
# Infrastruktur + Frontend mit Hot Reload (hier kein Backend-Container)
docker compose -f docker/docker-compose-dev.yml up
```

Das Backend läuft aus der IDE oder per CLI:

```bash
cp backend/run.env.example backend/run.env      # bei Bedarf anpassen
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Das Arbeitsverzeichnis muss `backend/` sein; `run.env` wird über
`spring.config.import` geladen (siehe Kommentare in `run.env.example`).

```bash
# Tests
cd backend  && ./gradlew test
cd frontend && npm test

# Vollständige Backend-Prüfung (nach jeder Backend-Änderung verbindlich)
cd backend && ./gradlew clean build
```

## 🐳 Compose-Dateien

| Datei | Verwendung |
| --- | --- |
| `docker/docker-compose.yml` | Produktion beim Kunden (veröffentlichte `ghcr.io`-Images) |
| `docker/docker-compose-local.yml` | Produktionsnah, mit lokal gebauten Images |
| `docker/docker-compose-dev.yml` | Entwicklung — Frontend mit Hot Reload, Backend aus der IDE |
| `docker/docker-compose*.ssl.yml` | Jeweiliger Scope mit HTTPS |
| `docker/docker-compose-restore-test.yml` | Isolierter Datenbank-Restore-Test |

## 🗄️ Backups

Der `backup`-Dienst erstellt einen logischen MySQL-Dump zur konfigurierten
Uhrzeit (`BACKUP_TIME` / `BACKUP_TIMEZONE`), alle `BACKUP_INTERVAL_DAYS` Tage. Er
hält eine lokale Aufbewahrungsfrist, prüft Prüfsummen und schreibt Statusdateien,
die die Ansicht **Einstellungen → Datensicherung** in der Anwendung liest (Status,
Verlauf, nächster Lauf, konkrete Handlungshinweise bei Fehlern). Restore und
Restore-Test laufen über `scripts/database_restore.sh` bzw.
`scripts/database_restore_test.sh`.

- Betrieb & Konfiguration → [`docs/backup-operation.md`](docs/backup-operation.md)
- Umstellung einer bestehenden Installation → [`docs/backup-migration.md`](docs/backup-migration.md)
- Wiederherstellung eines Backups vor 3.1 (`.tgz` / `.sql.gz` / `.sql`) → Abschnitt *„Wiederherstellung einer alten Sicherung"* in `docs/backup-operation.md`

## 🔧 Skripte

| Skript | Zweck |
| --- | --- |
| `scripts/init.sh` | Einmalige Erstinitialisierung: Standard-Secrets + JWT-Schlüssel |
| `scripts/build_local_images.sh` | Baut die `:local`-Images für Backend/Frontend |
| `scripts/database_create_backup_user.sh` | Legt den eingeschränkten Backup-Datenbankbenutzer an bzw. aktualisiert ihn |
| `scripts/database_backup.sh` | Startet einen sofortigen Backup-Lauf im laufenden Container |
| `scripts/database_restore.sh` | Spielt einen Dump in die Produktivdatenbank ein (`.sql.gz` / `.sql` / `.tgz`) |
| `scripts/database_restore_test.sh` | Importiert einen Dump in eine wegwerfbare MySQL-Instanz und prüft ihn |
| `scripts/database_remove_db_volume.sh` | Löscht den lokalen Datenbank-Container und das Volume (mit Rückfrage) |

## 📚 Dokumentation

- [`docs/backup-operation.md`](docs/backup-operation.md) · [`docs/backup-migration.md`](docs/backup-migration.md) — Backups
- [`docs/logging-guide.md`](docs/logging-guide.md) — Logging-Regeln (Pflichtfelder, Log-Level, Audit-/Security-Trennung, Aufbewahrung)
- [`AGENTS.md`](AGENTS.md) — verbindliche Projekt- und Beitragsregeln
- [`CHANGELOG.md`](CHANGELOG.md) — Release-Notes

## 🤝 Mitwirken

Siehe [`CONTRIBUTING.md`](CONTRIBUTING.md) und die verbindlichen Regeln in
[`AGENTS.md`](AGENTS.md). Fachliche Regeln und Validierungen gehören ins Backend;
das Frontend ist Angular (React erst bei einer ausdrücklich beauftragten
Migration).

## 📄 Lizenz

Lizenziert unter der **GPLv3** — siehe [`LICENSE`](LICENSE).
