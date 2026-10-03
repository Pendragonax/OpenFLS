package de.vinz.openfls.domains.logging.dto

data class LogEntryResponse(
    val timestamp: String,
    val level: String,
    val logger: String,
    val thread: String,
    val message: String,
    /** Multi-line exception stacktrace belonging to this entry, or null when there is none. */
    val stacktrace: String? = null
)
