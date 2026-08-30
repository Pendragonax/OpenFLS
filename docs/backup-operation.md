# OpenFLS-Datensicherung: Betrieb

## Einfaches Betriebsmodell

Der Compose-Service `backup` startet zusammen mit OpenFLS und bleibt dauerhaft
aktiv. Er erstellt beim Start einen logischen MySQL-Dump und danach alle sechs
Stunden. Bei einem Fehler wartet er fünf Minuten und versucht es erneut.

Es sind keine Systemd-Dienste, Host-Timer oder zusätzlichen Konfigurationsdateien
erforderlich. Die Backups liegen eindeutig unter `docker/backup`, relativ zur
jeweiligen Compose-Datei.

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
   individuellen Passwort und restriktiven Dateirechten an.
2. Erstelle das Datenbankkonto einmalig, während der Stack läuft:

   ```bash
   MYSQL_BACKUP_PASSWORD_FILE=secrets/db_backup_password.secret \
     scripts/database_create_backup_user.sh
   ```

3. Starte oder aktualisiere den Stack wie gewohnt:

   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

Der Service nutzt ausschließlich `openfls_backup`, niemals den MySQL-
Root-Account. Er hat nur Leserechte auf das Schema `openfls` sowie die zum
Dumpen nötigen Rechte für Views und Trigger.

## Status und Fehlererkennung

`status/latest.json` enthält das jüngste Ergebnis; `history.jsonl` hält bis zu
1.000 abgeschlossene Läufe fest. Beide Dateien enthalten weder Zugangsdaten noch
Fachdaten und sind die spätere Datenquelle für die OpenFLS-Oberfläche.

Der Backup-Container erhält den Status `unhealthy`, wenn kein erfolgreiches
Backup vorliegt, ein Lauf fehlgeschlagen ist oder das letzte erfolgreiche Backup
älter als sieben Stunden ist. Die zuständige Administration kann den Zustand und
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

- `BACKUP_INTERVAL_SECONDS=21600` – reguläres Intervall (sechs Stunden)
- `BACKUP_RETRY_INTERVAL_SECONDS=300` – Wiederholung nach Fehler (fünf Minuten)
- `BACKUP_RETENTION_DAYS=14` – lokale Aufbewahrung
- `BACKUP_MAX_AGE_HOURS=7` – Grenze für den Healthcheck

## Grenzen

Die aktuelle OpenFLS-Datenbank enthält keine Routinen oder Events. Werden solche
Objekte später eingeführt, muss der Dump-Aufruf bewusst erweitert werden; MySQL
benötigt dafür zusätzliche globale Leserechte.

Ein Backup gilt erst nach einem isolierten Restore-Test als belastbar. Dieser
wird als separater nächster Baustein implementiert und erhält ein eigenes,
temporäres MySQL-Volume. Der Produktiv-Restore bleibt bis dahin ein berechtigter
Administrationsvorgang.

Für Bestandskunden beschreibt [backup-migration.md](backup-migration.md) die
sichere Umstellung auf den neuen Service vor einem OpenFLS-Release.
