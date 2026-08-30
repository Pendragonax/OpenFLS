package de.vinz.openfls.domains.backup.dto

/**
 * One completed backup or restore-test run, read from the backup service status
 * files. All fields are nullable because the status files are written by an
 * independent shell job and may be missing or partially written while a poll
 * happens.
 */
data class BackupRunDto(
    val timestamp: String?,
    /** `success`, `failure` or `null` when unknown. */
    val outcome: String?,
    val message: String?,
    val backupFile: String?,
    val sizeBytes: Long?,
    val sha256: String?,
    val durationSeconds: Long?,
    /**
     * Machine-readable failure cause set by the backup job:
     * `backup_user_missing`, `database_unreachable`, `insufficient_grants`,
     * `backup_secret_missing`, `unknown`, or `null` on success.
     */
    val reason: String?
)

/**
 * Effective operational configuration of the backup service, written by the
 * service itself to `status/config.json`. Nullable throughout so an older
 * service without the file still yields a valid response.
 */
data class BackupConfigDto(
    val database: String?,
    /** Seconds between scheduled backups. */
    val intervalSeconds: Long?,
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

/**
 * Aggregated read-only view for the "Datensicherung" dashboard. It never exposes
 * dump contents, credentials or client data - only operational metadata.
 */
data class BackupStatusDto(
    val lastBackup: BackupRunDto?,
    val lastRestoreTest: BackupRunDto?,
    /** true when there is no successful backup within [BackupConfigDto.maxAgeHours]. */
    val backupOverdue: Boolean,
    val maxAgeHours: Long,
    /** `ok`, `overdue`, `failed` or `unknown`. */
    val overall: String,
    val config: BackupConfigDto?
)

/** One entry of the merged backup / restore-test history. */
data class BackupHistoryEntryDto(
    /** `backup` or `restore_test`. */
    val kind: String,
    val timestamp: String?,
    val outcome: String?,
    val message: String?,
    val backupFile: String?,
    val sizeBytes: Long?,
    val sha256: String?,
    val durationSeconds: Long?
)
