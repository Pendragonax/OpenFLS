package de.vinz.openfls.domains.clientTasks.dto

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Read model of a client task. Employees are reduced to id and name so that no
 * further personal data leaves the service boundary.
 */
data class ClientTaskResponse(
    val id: Long,
    val clientId: Long,
    val title: String,
    val description: String,
    val dueDate: LocalDate,
    val createdAt: LocalDateTime,
    val createdById: Long?,
    val createdByName: String,
    val done: Boolean,
    val overdue: Boolean,
    val completedById: Long?,
    val completedByName: String?,
    val completedOn: LocalDate?,
    val completedAt: LocalDateTime?,
    val completionComment: String?
)
