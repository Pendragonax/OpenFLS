package de.vinz.openfls.domains.logging.dto

data class LogEntryDto(
    val timestamp: String,
    val level: String,
    val logger: String,
    val thread: String,
    val message: String
)

data class LogLevelDto(val logger: String, val level: String?)

data class LogSettingsDto(val rootLevel: String, val classLevels: List<LogLevelDto>)

data class LogQueryDto(
    val from: String? = null,
    val to: String? = null,
    val query: String? = null,
    val level: String? = null,
    val logger: String? = null,
    val thread: String? = null,
    val all: Boolean = false
)
