package de.vinz.openfls.domains.clientTasks.service

import de.vinz.openfls.domains.clientTasks.dto.ClientTaskAuditLogResponse
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompletedPageResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCountDto
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskDeleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskPageResponse
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskResponse
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateResult
import de.vinz.openfls.domains.clientTasks.entity.ClientTask
import de.vinz.openfls.domains.clientTasks.entity.ClientTaskAuditAction
import de.vinz.openfls.domains.clientTasks.entity.ClientTaskAuditLog
import de.vinz.openfls.domains.clientTasks.repository.ClientTaskAuditLogRepository
import de.vinz.openfls.domains.clientTasks.repository.ClientTaskRepository
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.logging.StructuredLog
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Use cases around client tasks. Every mutation writes an audit entry. Completed
 * tasks are immutable but may still be deleted.
 */
@Service
class ClientTaskService(
    private val clientTaskRepository: ClientTaskRepository,
    private val clientTaskAuditLogRepository: ClientTaskAuditLogRepository,
    private val clientService: ClientService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService,
    private val clock: Clock
) {

    private data class Actor(val employee: Employee, val name: String)

    @Transactional(readOnly = true)
    fun getOpenTasksByClientId(clientId: Long): List<ClientTaskResponse>? {
        if (!clientService.existsById(clientId)) {
            return null
        }

        val today = LocalDate.now(clock)
        return clientTaskRepository.findAllByClientIdAndDoneOrderByDueDateAscIdAsc(clientId, false)
            .map { toResponse(it, today) }
    }

    @Transactional(readOnly = true)
    fun getCompletedTasksByClientId(clientId: Long, page: Int, size: Int): ClientTaskCompletedPageResult {
        if (!clientService.existsById(clientId)) {
            return ClientTaskCompletedPageResult.ClientNotFound
        }
        if (page < 0 || size !in 1..100) {
            return ClientTaskCompletedPageResult.InvalidPagination
        }

        val result = clientTaskRepository.findAllByClientIdAndDoneOrderByCompletedAtDescIdDesc(
            clientId, true, PageRequest.of(page, size)
        )
        val today = LocalDate.now(clock)
        return ClientTaskCompletedPageResult.Success(
            ClientTaskPageResponse(
                result.content.map { toResponse(it, today) },
                result.number,
                result.size,
                result.totalElements,
                result.totalPages
            )
        )
    }

    @Transactional(readOnly = true)
    fun getOpenTaskCountsByClientIds(clientIds: List<Long>): Map<Long, ClientTaskCountDto> {
        if (clientIds.isEmpty()) {
            return emptyMap()
        }
        return clientTaskRepository
            .findOpenTaskCountsByClientIds(clientIds, LocalDate.now(clock))
            .associateBy { it.clientId }
    }

    @Transactional(readOnly = true)
    fun getAuditHistoryByTaskId(taskId: Long): List<ClientTaskAuditLogResponse>? {
        if (!clientTaskRepository.existsById(taskId)) {
            return null
        }

        return clientTaskAuditLogRepository.findAllByClientTaskIdOrderByChangedAtDesc(taskId)
            .filter { it.action == ClientTaskAuditAction.UPDATE || it.action == ClientTaskAuditAction.COMPLETE }
            .map { toResponse(it) }
    }

    @Transactional
    fun create(request: ClientTaskCreateRequest): ClientTaskCreateResult {
        val client = clientService.getEntityById(request.clientId)
            ?: return ClientTaskCreateResult.ClientNotFound
        val actor = currentActor()

        val now = LocalDateTime.now(clock)
        val saved = clientTaskRepository.save(
            ClientTask(
                client = client,
                title = request.title.trim(),
                description = request.description.trim(),
                dueDate = request.dueDate,
                createdBy = actor.employee,
                createdAt = now,
                done = false
            )
        )

        writeAuditLog(
            task = saved,
            action = ClientTaskAuditAction.CREATE,
            actorId = actor.employee.id,
            actorName = actor.name,
            changedAt = now,
            afterTitle = saved.title,
            afterDescription = saved.description,
            afterDueDate = saved.dueDate,
            afterDone = false
        )
        StructuredLog.audit("client.task.created", "success", "client.task", saved.id.toString())

        return ClientTaskCreateResult.Success(toResponse(saved, LocalDate.now(clock)))
    }

    @Transactional
    fun update(id: Long, request: ClientTaskUpdateRequest): ClientTaskUpdateResult {
        val task = clientTaskRepository.findByIdOrNull(id)
            ?: return ClientTaskUpdateResult.NotFound
        if (task.done) {
            return ClientTaskUpdateResult.AlreadyCompleted
        }
        val actor = currentActor()

        val beforeTitle = task.title
        val beforeDescription = task.description
        val beforeDueDate = task.dueDate

        task.title = request.title.trim()
        task.description = request.description.trim()
        task.dueDate = request.dueDate

        val saved = clientTaskRepository.save(task)

        writeAuditLog(
            task = saved,
            action = ClientTaskAuditAction.UPDATE,
            actorId = actor.employee.id,
            actorName = actor.name,
            changedAt = LocalDateTime.now(clock),
            beforeTitle = beforeTitle,
            afterTitle = saved.title,
            beforeDescription = beforeDescription,
            afterDescription = saved.description,
            beforeDueDate = beforeDueDate,
            afterDueDate = saved.dueDate
        )
        StructuredLog.audit("client.task.updated", "success", "client.task", saved.id.toString())

        return ClientTaskUpdateResult.Success(toResponse(saved, LocalDate.now(clock)))
    }

    @Transactional
    fun complete(id: Long, request: ClientTaskCompleteRequest): ClientTaskCompleteResult {
        val task = clientTaskRepository.findByIdOrNull(id)
            ?: return ClientTaskCompleteResult.NotFound
        if (task.done) {
            return ClientTaskCompleteResult.AlreadyCompleted
        }
        val actor = currentActor()

        val now = LocalDateTime.now(clock)
        task.done = true
        task.completedBy = actor.employee
        task.completedAt = now
        task.completedOn = request.completedOn
        task.completionComment = request.comment.trim()

        val saved = clientTaskRepository.save(task)

        writeAuditLog(
            task = saved,
            action = ClientTaskAuditAction.COMPLETE,
            actorId = actor.employee.id,
            actorName = actor.name,
            changedAt = now,
            beforeDone = false,
            afterDone = true,
            comment = saved.completionComment
        )
        StructuredLog.audit("client.task.completed", "success", "client.task", saved.id.toString())

        return ClientTaskCompleteResult.Success(toResponse(saved, LocalDate.now(clock)))
    }

    @Transactional
    fun delete(id: Long): ClientTaskDeleteResult {
        val task = clientTaskRepository.findByIdOrNull(id)
            ?: return ClientTaskDeleteResult.NotFound
        val actor = currentActor()
        val response = toResponse(task, LocalDate.now(clock))

        writeAuditLog(
            task = task,
            action = ClientTaskAuditAction.DELETE,
            actorId = actor.employee.id,
            actorName = actor.name,
            changedAt = LocalDateTime.now(clock),
            beforeTitle = task.title,
            beforeDescription = task.description,
            beforeDueDate = task.dueDate,
            beforeDone = task.done
        )
        StructuredLog.audit("client.task.deleted", "success", "client.task", task.id.toString())

        clientTaskRepository.delete(task)

        return ClientTaskDeleteResult.Success(response)
    }

    /**
     * Removes every task of a client, e.g. when the client itself is deleted. Each
     * removal is recorded so the audit trail stays complete.
     */
    @Transactional
    fun deleteAllByClientId(clientId: Long, actorId: Long, actorName: String) {
        val tasks = clientTaskRepository.findAllByClientId(clientId)
        if (tasks.isEmpty()) {
            return
        }

        val now = LocalDateTime.now(clock)
        tasks.forEach { task ->
            writeAuditLog(
                task = task,
                action = ClientTaskAuditAction.DELETE,
                actorId = actorId,
                actorName = actorName,
                changedAt = now,
                beforeTitle = task.title,
                beforeDescription = task.description,
                beforeDueDate = task.dueDate,
                beforeDone = task.done
            )
        }
        StructuredLog.audit("client.task.deleted.bulk", "success", "client", clientId.toString())
        clientTaskRepository.deleteAll(tasks)
    }

    private fun currentActor(): Actor {
        val employee = employeeService.getEntityById(accessService.getId())
            ?: throw IllegalStateException("current user not found")
        return Actor(employee, employee.displayName())
    }

    private fun writeAuditLog(
        task: ClientTask,
        action: ClientTaskAuditAction,
        actorId: Long?,
        actorName: String,
        changedAt: LocalDateTime,
        beforeTitle: String? = null,
        afterTitle: String? = null,
        beforeDescription: String? = null,
        afterDescription: String? = null,
        beforeDueDate: LocalDate? = null,
        afterDueDate: LocalDate? = null,
        beforeDone: Boolean? = null,
        afterDone: Boolean? = null,
        comment: String? = null
    ) {
        clientTaskAuditLogRepository.save(
            ClientTaskAuditLog(
                clientTaskId = task.id,
                clientId = task.client?.id ?: 0,
                action = action,
                changedAt = changedAt,
                actorEmployeeId = actorId,
                actor = actorName.take(128),
                beforeTitle = beforeTitle,
                afterTitle = afterTitle,
                beforeDescription = beforeDescription,
                afterDescription = afterDescription,
                beforeDueDate = beforeDueDate,
                afterDueDate = afterDueDate,
                beforeDone = beforeDone,
                afterDone = afterDone,
                comment = comment
            )
        )
    }

    private fun toResponse(task: ClientTask, today: LocalDate): ClientTaskResponse {
        return ClientTaskResponse(
            id = task.id,
            clientId = task.client?.id ?: 0,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            createdAt = task.createdAt,
            createdById = task.createdBy?.id,
            createdByName = task.createdBy.displayName(),
            done = task.done,
            overdue = !task.done && task.dueDate.isBefore(today),
            completedById = task.completedBy?.id,
            completedByName = task.completedBy?.displayName(),
            completedOn = task.completedOn,
            completedAt = task.completedAt,
            completionComment = task.completionComment
        )
    }

    private fun toResponse(log: ClientTaskAuditLog): ClientTaskAuditLogResponse {
        return ClientTaskAuditLogResponse(
            id = log.id,
            clientTaskId = log.clientTaskId,
            action = log.action,
            changedAt = log.changedAt,
            actor = log.actor,
            beforeTitle = log.beforeTitle,
            afterTitle = log.afterTitle,
            beforeDescription = log.beforeDescription,
            afterDescription = log.afterDescription,
            beforeDueDate = log.beforeDueDate,
            afterDueDate = log.afterDueDate,
            beforeDone = log.beforeDone,
            afterDone = log.afterDone,
            comment = log.comment
        )
    }

    private fun Employee?.displayName(): String {
        if (this == null) {
            return "Unbekannt"
        }
        return "${this.firstname} ${this.lastname}".trim().ifBlank { "Unbekannt" }
    }
}
