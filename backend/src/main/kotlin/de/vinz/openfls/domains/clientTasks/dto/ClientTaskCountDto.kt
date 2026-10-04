package de.vinz.openfls.domains.clientTasks.dto

/**
 * Number of open and already overdue tasks of one client.
 */
data class ClientTaskCountDto(
    val clientId: Long,
    val openCount: Long,
    val overdueCount: Long
)
