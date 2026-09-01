# MySQL Backup und Restore

Die bisherige automatische Sicherung wird durch den Compose-Service `backup`
ersetzt. Die aktuelle Betriebsanleitung
einschließlich Installation, Status, Alarmierung und Kundenpflichten steht in
[docs/backup-operation.md](docs/backup-operation.md).

Für bestehende Kundeninstallationen steht der verbindliche Umstellungsablauf in
[docs/backup-migration.md](docs/backup-migration.md).

OpenFLS zeigt unter `Einstellungen` (Bereiche Protokolle / Datensicherung, nur
Administrationsrolle) eine Nur-Lese-Statusübersicht des Backup-Dienstes.

Der bestehende manuelle Produktiv-Restore bleibt bis zur Einführung des
separaten Restore-Test- und Restore-Prozesses ein ausschließlich berechtigter
Administrationsvorgang.
