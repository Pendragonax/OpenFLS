package de.vinz.openfls.domains.clientTasks.dtos

import de.vinz.openfls.domains.clientTasks.ClientTaskAuditAction
import java.time.LocalDate
import java.time.LocalDateTime

data class ClientTaskAuditLogDto(
    val id: Long,
    val clientTaskId: Long,
    val action: ClientTaskAuditAction,
    val changedAt: LocalDateTime,
    val actor: String,
    val beforeTitle: String?,
    val afterTitle: String?,
    val beforeDescription: String?,
    val afterDescription: String?,
    val beforeDueDate: LocalDate?,
    val afterDueDate: LocalDate?,
    val beforeDone: Boolean?,
    val afterDone: Boolean?,
    val comment: String?
)
