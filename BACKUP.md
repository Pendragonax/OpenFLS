# MySQL Backup and Restore

The former backup container is replaced by the Compose service `backup`. The current operating instructions,
including installation, status, alerting, and customer responsibilities, are in
[docs/backup-operation.md](docs/backup-operation.md).

For existing customer installations, follow
[docs/backup-migration.md](docs/backup-migration.md).

OpenFLS shows a read-only status view of the backup service under
`Einstellungen` (Protokolle / Datensicherung, admin role only).

The existing manual production restore remains a privileged administrative
procedure until the separate restore-test and restore workflow is introduced.
