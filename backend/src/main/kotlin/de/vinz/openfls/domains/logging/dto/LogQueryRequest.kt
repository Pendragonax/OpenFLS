package de.vinz.openfls.domains.logging.dto

data class LogQueryRequest(
    val from: String? = null,
    val to: String? = null,
    val query: String? = null,
    val level: String? = null,
    val logger: String? = null,
    val thread: String? = null,
    val all: Boolean = false
)
