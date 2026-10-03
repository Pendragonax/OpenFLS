package de.vinz.openfls.domains.backup.dto

/**
 * Aggregated read-only view for the "Datensicherung" dashboard. It never exposes
 * dump contents, credentials or client data - only operational metadata.
 */
data class BackupStatusResponse(
    val lastBackup: BackupRunResponse?,
    val lastRestoreTest: BackupRunResponse?,
    /** true when there is no successful backup within [BackupConfigResponse.maxAgeHours]. */
    val backupOverdue: Boolean,
    val maxAgeHours: Long,
    /** `ok`, `overdue`, `failed` or `unknown`. */
    val overall: String,
    val config: BackupConfigResponse?,
    /** ISO-8601 instant of the next scheduled daily backup, or `null` when unknown. */
    val nextExpectedBackup: String?
)
