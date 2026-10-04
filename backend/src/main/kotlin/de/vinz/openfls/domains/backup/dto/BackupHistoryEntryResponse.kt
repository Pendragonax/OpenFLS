package de.vinz.openfls.domains.backup.dto

/** One entry of the merged backup / restore-test history. */
data class BackupHistoryEntryResponse(
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
