package de.vinz.openfls.domains.backup.dto

/**
 * Effective operational configuration of the backup service, written by the
 * service itself to `status/config.json`. Nullable throughout so an older
 * service without the file still yields a valid response.
 */
data class BackupConfigResponse(
    val database: String?,
    /** Local time of day the backup runs, `HH:MM`. */
    val backupTime: String?,
    /** IANA timezone [backupTime] is interpreted in, e.g. `Europe/Berlin`. */
    val timezone: String?,
    /** Whole days between two backups (>= 1); 1 means daily. */
    val intervalDays: Long?,
    /** Seconds the scheduler waits after a failed run before retrying. */
    val retryIntervalSeconds: Long?,
    /** Local retention of dump files, in days. */
    val retentionDays: Long?,
    /** Maximum number of entries kept in the technical history file. */
    val historyMaxEntries: Long?,
    /** Age after which a successful backup counts as overdue (healthcheck limit). */
    val maxAgeHours: Long?,
    /** Age after which a held lock is treated as stale and broken. */
    val staleLockSeconds: Long?,
    val generatedAt: String?
)
