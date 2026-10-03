package de.vinz.openfls.domains.logging.dto

data class LogSettingsResponse(val rootLevel: String, val classLevels: List<LogLevelResponse>)
