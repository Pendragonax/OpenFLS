# Migration bestehender OpenFLS-Installationen auf den Backup-Service

Dieser Ablauf migriert eine vorhandene Kundeninstallation vom bisherigen
`databack/mysql-backup`-Container auf den Compose-Service `backup`. Er verändert
weder das MySQL-Daten-Volume noch OpenFLS-Fachdaten. Die einzige
Datenbankänderung ist das zusätzliche, nur lesende Konto `openfls_backup`.

Der Ablauf ist vor **jedem** OpenFLS-Release auszuführen, das den neuen
Backup-Service erstmals enthält. Erst nach einem erfolgreich verifizierten neuen
Dump darf das übrige OpenFLS-Update einschließlich möglicher Flyway-Migrationen
gestartet werden.

## Voraussetzungen

- Die zuständige Administration kennt die installierte Release-Version und den
  verwendeten Compose-Scope (Produktion, lokal oder SSL).
- Das aktuelle Backup-Verzeichnis `docker/backup` und die alten `.tgz`-Dateien
  werden vorab an den vom Kunden bestimmten sicheren Ort kopiert. Die alte
  Aufbewahrung wird während der Migration nicht gelöscht.
- Es ist ausreichend freier Speicher für mindestens einen vollständigen neuen
  Dump vorhanden.
- Für das geplante OpenFLS-Release existiert ein dokumentierter Rückweg. Bei
  Datenbankmigrationen gelten zusätzlich die release-spezifischen Hinweise.

## Migrationsablauf

1. **Vorherigen Stand festhalten.**
   Notiere Release-Version, Compose-Datei und den Zustand aller Dienste. Prüfe,
   ob mindestens eine bisherige Sicherung vorhanden ist. Kopiere die bisherigen
   Sicherungen extern oder in das dafür bestimmte Kundenarchiv.

2. **Neue Release-Dateien bereitstellen, aber die Anwendung noch nicht
   aktualisieren.**
   Der laufende MySQL-Container und das Daten-Volume bleiben unverändert.

3. **Backup-Secret und Statusverzeichnis anlegen.**
   Lege in der Installation `secrets/db_backup_password.secret` mit einem
   zufällig erzeugten, ausschließlich für Backups verwendeten Passwort an.
   Die Datei darf nur für die zuständige Administration lesbar sein und wird
   nicht versioniert. Lege außerdem das Statusverzeichnis an, damit Docker den
   Bind-Mount-Pfad nicht als `root` erzeugt:

   ```bash
   mkdir -p docker/backup/status
   ```

4. **Backup-Konto auf der noch laufenden Datenbank einrichten.**

   ```bash
   MYSQL_BACKUP_PASSWORD_FILE=secrets/db_backup_password.secret \
     scripts/database_create_backup_user.sh
   ```

   Der Befehl verwendet nur während dieses Schritts den bereits vorhandenen
   MySQL-Root-Zugang. Er gibt dem neuen Konto ausschließlich Leserechte auf
   `openfls` sowie Rechte für Views und Trigger.

5. **Nur den neuen Backup-Service starten.**
   Verwende dieselbe Compose-Datei wie im produktiven Betrieb. Der Parameter
   `--no-deps` verhindert, dass dabei Datenbank, Backend oder Frontend neu
   gestartet werden.

   ```bash
   docker compose -f docker/docker-compose.yml up -d --no-deps backup
   ```

6. **Ersten neuen Dump prüfen.**
   Warte den ersten Lauf ab und prüfe:

   ```bash
   docker compose -f docker/docker-compose.yml ps backup
   docker compose -f docker/docker-compose.yml logs backup
   ```

   Der Service muss `healthy` werden. Zusätzlich müssen in
   `docker/backup/status/latest.json` ein erfolgreicher Lauf sowie eine neue
   `.sql.gz`-Datei mit passender `.sha256`-Datei stehen. Bei einem Fehler wird
   nicht mit dem Anwendungsupdate fortgefahren.

   Die Prüfsumme lässt sich zusätzlich im Backup-Verzeichnis verifizieren:

   ```bash
   cd docker/backup && sha256sum -c <neue-backup-datei>.sql.gz.sha256
   ```

7. **Erst jetzt das eigentliche OpenFLS-Release aktualisieren.**
   Images laden und den Stack nach dem für das Release vorgesehenen Verfahren
   aktualisieren. Der Backup-Service läuft dabei weiter.

8. **Abnahme dokumentieren.**
   Halte Zeitpunkt, neue Version, Ergebnis des ersten Dumps, verantwortliche
   Person und eventuelle Abweichungen im Kunden-Betriebsprotokoll fest.

## Rückweg

Die Migration des Backup-Service allein lässt sich zurücknehmen, indem die
vorherige Release-Version mit ihrer ursprünglichen Compose-Datei wieder
bereitgestellt und der Stack ohne `-v` aktualisiert wird. Das Daten-Volume wird
dabei nie gelöscht. Das zusätzliche Backup-Konto kann bestehen bleiben; es
stört den vorherigen Betrieb nicht.

Wichtig: Startet das neue OpenFLS-Backend nach Schritt 7 Datenbankmigrationen,
ist ein Rücksprung auf ein älteres Backend nur zulässig, wenn dessen
Release-Hinweise die Rückwärtskompatibilität bestätigen. Andernfalls erfolgt
eine Wiederherstellung aus der vor Schritt 7 gesicherten Datenbank nach dem
vorgesehenen Restore-Verfahren. Deshalb ist Schritt 6 eine harte
Freigabeschwelle.

## Alte Sicherungen

Die neue Aufräumroutine entfernt ausschließlich ihre eigenen `.sql.gz`-Dateien
und zugehörige Metadaten. Vorhandene alte `.tgz`-Sicherungen bleiben erhalten.
Ihre Übernahme in das externe Kundenarchiv oder ihre kontrollierte Löschung wird
erst nach dem isolierten Restore-Test entschieden.
