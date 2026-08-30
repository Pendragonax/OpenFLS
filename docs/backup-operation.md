# OpenFLS-Datensicherung: Betrieb

## Einfaches Betriebsmodell

Der Compose-Service `backup` startet zusammen mit OpenFLS und bleibt dauerhaft
aktiv. Er prüft jeden Tag zur konfigurierten Uhrzeit (`BACKUP_TIME`, Standard
`02:30` in `BACKUP_TIMEZONE`, Standard `Europe/Berlin`), ob seit dem letzten
erfolgreichen Backup mindestens `BACKUP_INTERVAL_DAYS` Kalendertage vergangen
sind, und erstellt in diesem Fall genau einen logischen MySQL-Dump.
`BACKUP_INTERVAL_DAYS=1` bedeutet täglich, `2` alle 48 Stunden usw.; Werte
kleiner als `1` gelten als `1`. Bei einem Fehler wird alle
`BACKUP_RETRY_INTERVAL_SECONDS` (Standard fünf Minuten) wiederholt, bis der Lauf
erfolgreich ist. Ist beim Start bereits ein Backup fällig (oder liegt noch
keines vor), wird es sofort einmal nachgeholt.

Es sind keine Systemd-Dienste, Host-Timer oder zusätzlichen Konfigurationsdateien
erforderlich. Die Backups liegen eindeutig unter `docker/backup`, relativ zur
jeweiligen Compose-Datei.

Der Container bindet das gesamte `scripts/`-Verzeichnis schreibgeschützt unter
`/opt/openfls-scripts` ein (kein Einzeldatei-Mount – der spiegelt Host-Änderungen
nach einem atomaren Editor-Save erst nach Container-Neustart). Nach einer
Änderung an den Backup-Skripten reicht `docker compose … restart backup`.

```text
docker/backup/
  <zeitpunkt>-openfls.sql.gz
  <zeitpunkt>-openfls.sql.gz.sha256
  <zeitpunkt>-openfls.json
  status/latest.json
  status/history.jsonl
```

Die Dump-Dateien enthalten besonders schützenswerte Daten. Das Verzeichnis darf
nicht öffentlich erreichbar sein. Die Kundenorganisation ist für Verschlüsselung,
externe Kopie und deren Aufbewahrung verantwortlich.

## Einmalige Einrichtung

1. Lege `secrets/db_backup_password.secret` mit einem zufällig erzeugten,
   individuellen Passwort und restriktiven Dateirechten an. `scripts/database_create_secrets.sh`
   erzeugt dieses Secret automatisch (via `openssl rand`) und legt zugleich das
   Verzeichnis `docker/backup/status` an.
2. Stelle sicher, dass `docker/backup/status` **vor** dem ersten
   `docker compose up` existiert und dem ausführenden Benutzer gehört. Andernfalls
   legt Docker den Bind-Mount-Pfad als `root` an und weder der Backup-Container
   noch das schreibgeschützte Backend-Mount funktionieren:

   ```bash
   mkdir -p docker/backup/status
   ```

3. Erstelle das Datenbankkonto einmalig, während der Stack läuft:

   ```bash
   MYSQL_BACKUP_PASSWORD_FILE=secrets/db_backup_password.secret \
     scripts/database_create_backup_user.sh
   ```

4. Starte oder aktualisiere den Stack wie gewohnt:

   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

Der Service nutzt ausschließlich `openfls_backup`, niemals den MySQL-
Root-Account. Er hat nur Leserechte auf das Schema `openfls` sowie die zum
Dumpen nötigen Rechte für Views und Trigger.

## Status und Fehlererkennung

`status/latest.json` enthält das jüngste Ergebnis; `status/history.jsonl` hält
bis zu 1.000 abgeschlossene Läufe fest (eine JSON-Zeile je Lauf, mit
abschließendem Zeilenumbruch); `status/config.json` enthält die effektive
Betriebskonfiguration (Backup-Uhrzeit, Zeitzone, Abstand in Tagen, Aufbewahrung, Grenzwerte) und wird beim Start
des Backup-Dienstes geschrieben. Diese Dateien enthalten weder Zugangsdaten noch
Fachdaten und sind die Datenquelle für die OpenFLS-Oberfläche.

Bei einem Fehler enthält der Statuseintrag zusätzlich `reason` mit einer groben
Klassifizierung (`backup_user_missing`, `backup_secret_missing`,
`insufficient_grants`, `database_unreachable`, `unknown`). Vor dem Dump prüft der
Job die Anmeldung des Backup-Kontos mit einem `SELECT 1`; schlägt sie fehl,
steht `reason=backup_user_missing` deterministisch fest (statt einen generischen
mysqldump-Fehler zu interpretieren), und OpenFLS verweist direkt auf
`scripts/database_create_backup_user.sh`. Der rohe Fehlertext wird nur
ausgewertet, nicht gespeichert.

Jedes JSON-Objekt (`latest.json`, jede `history.jsonl`-Zeile, `config.json`,
die Restore-Test-Dateien, die `<run_id>.json`) trägt `schema_version` (aktuell
`1`). Fehlt das Feld, gilt Version 1. Der Leser im Backend verarbeitet auch eine
höhere Version tolerant, protokolliert sie aber (`event_name=
backup.schema.unsupported`), damit ein Producer/Consumer-Versatz nach einem
Teil-Upgrade sichtbar wird. `schema_version` wird nur bei brechenden Änderungen
erhöht, nicht bei rein additiven Feldern.

Der Backup-Container erhält den Status `unhealthy`, wenn kein erfolgreiches
Backup vorliegt, ein Lauf fehlgeschlagen ist oder das letzte erfolgreiche Backup
älter als `BACKUP_MAX_AGE_HOURS` (Standard 26 Stunden) ist. Die zuständige Administration kann den Zustand und
die strukturierten Logs mit folgenden Standardbefehlen prüfen:

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs backup
```

Ein manueller Lauf verwendet den bereits laufenden Service:

```bash
scripts/database_backup.sh
```

Die Werte stehen zentral in `docker/docker-compose.env` und werden über
`env_file` vom `backup`-Service gelesen. Sie können dort bei Bedarf angepasst
werden:

- `BACKUP_TIME=02:30` – Uhrzeit des Backups (HH:MM, 24h)
- `BACKUP_TIMEZONE=Europe/Berlin` – Zeitzone, in der `BACKUP_TIME` gilt.
  Gültige Werte sind IANA-Zonennamen (z. B. `Europe/Berlin`, `UTC`); die
  vollständige Liste steht im Container unter `/usr/share/zoneinfo` bzw. unter
  <https://en.wikipedia.org/wiki/List_of_tz_database_time_zones> (Spalte
  „TZ identifier“).
- `BACKUP_INTERVAL_DAYS=1` – Abstand in ganzen Tagen zwischen zwei Backups
  (`1` = täglich, `2` = alle 48 h …); Werte < 1 gelten als 1
- `BACKUP_RETRY_INTERVAL_SECONDS=300` – Wiederholung nach Fehler (fünf Minuten)
- `BACKUP_STALE_LOCK_SECONDS=43200` – ab diesem Alter gilt eine Sperre als
  verwaist (z. B. nach einem harten Container-Absturz mitten im Dump) und wird
  automatisch gebrochen
- `BACKUP_RETENTION_DAYS=14` – lokale Aufbewahrung
- `BACKUP_MAX_AGE_HOURS=26` – Grenze für den Healthcheck (täglich + Puffer)

## Grenzen

Die aktuelle OpenFLS-Datenbank enthält keine Routinen oder Events. Werden solche
Objekte später eingeführt, muss der Dump-Aufruf bewusst erweitert werden; MySQL
benötigt dafür zusätzliche globale Leserechte.

Ein Backup kann mit einem isolierten Restore-Test geprüft werden:

```bash
scripts/database_restore_test.sh
```

Der Test verwendet standardmäßig die neueste `.sql.gz`-Sicherung, importiert sie
in eine eigene temporäre MySQL-Instanz ohne veröffentlichte Ports, prüft Tabellen
und Flyway-Historie und entfernt Container, Netzwerk und Test-Volume danach
wieder. Das Ergebnis steht in `status/restore-test-latest.json`. Eine bestimmte
Sicherung kann als erstes Argument übergeben werden. Der Produktiv-Restore
bleibt ein berechtigter Administrationsvorgang.

Für Bestandskunden beschreibt [backup-migration.md](backup-migration.md) die
sichere Umstellung auf den neuen Service vor einem OpenFLS-Release.

## OpenFLS-Oberfläche

OpenFLS bietet unter `Einstellungen` (nur für die Administrationsrolle) zwei
Bereiche, umschaltbar in der Kopfzeile: `Protokolle` (Standard) und
`Datensicherung` (`/settings/backup`). `Datensicherung` ist eine
**Nur-Lese-Übersicht** des Backup-Dienstes im Stil der Protokoll-Ansicht:

- Gesamtstatus (`Backup aktuell`, `überfällig`, `fehlgeschlagen`, `unbekannt`),
- Zeitpunkt, Größe, Dauer, Prüfsumme und nächste erwartete Sicherung,
- Ergebnis des letzten Restore-Tests,
- gemeldete Konfiguration aus `config.json` (Zeitplan mit Uhrzeit, Zeitzone und Tagesabstand, Aufbewahrung,
  Wiederholung nach Fehler, Überfälligkeitsgrenze, Verlaufsgröße),
- bei Fehlern einen konkreten Handlungshinweis, abgeleitet aus dem Feld
  `reason` des Backup-Jobs (`backup_user_missing`, `backup_secret_missing`,
  `insufficient_grants`, `database_unreachable`, `unknown`) – z. B. der Aufruf
  von `scripts/database_create_backup_user.sh`, wenn der Backup-Benutzer fehlt,
- einklappbare Erklärung, wie ein Restore auf dem System ausgeführt wird
  (`scripts/database_restore_test.sh`, dann `scripts/database_restore.sh`),
- Verlauf der letzten Backup- und Restore-Test-Läufe.

Die Seite liest ausschließlich die Statusdateien, nie die Dump-Dateien selbst.
Dafür wird `docker/backup/status` schreibgeschützt in den Backend-Container
gemountet (`/var/lib/openfls/backup-status`, Variable `BACKUP_STATUS_MOUNT`).
Weil Backend- und Backup-Container beide als UID 1000 laufen, kann das Backend
die mit `umask 077` geschriebenen Statusdateien lesen. Es gibt bewusst **keine**
Aktionen (kein Auslösen von Backup/Restore, kein Dump-Download) über die
Weboberfläche; jeder Aufruf wird als Audit-Ereignis (`backup.status.read`,
`backup.history.read`) protokolliert.

Läuft das Backend außerhalb von Docker (lokale Entwicklung), zeigt die Seite
`unbekannt`, solange `openfls.backup.status-directory` bzw. `BACKUP_STATUS_MOUNT`
nicht auf ein vorhandenes Statusverzeichnis zeigt (z. B.
`BACKUP_STATUS_MOUNT=../docker/backup/status`).
