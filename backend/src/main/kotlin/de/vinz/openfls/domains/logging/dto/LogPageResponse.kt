package de.vinz.openfls.domains.logging.dto

data class LogPageResponse(
    val content: List<LogEntryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean
)
