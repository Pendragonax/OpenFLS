package de.vinz.openfls.domains.backup.dto

/**
 * One completed backup or restore-test run, read from the backup service status
 * files. All fields are nullable because the status files are written by an
 * independent shell job and may be missing or partially written while a poll
 * happens.
 */
data class BackupRunResponse(
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
