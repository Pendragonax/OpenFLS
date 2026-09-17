package de.vinz.openfls.domains.clientTasks

import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskAuditLogDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskCountDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskPageDto
import de.vinz.openfls.domains.clientTasks.dtos.CompleteClientTaskDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskCreateDto
import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskUpdateDto
import de.vinz.openfls.domains.clientTasks.exceptions.ClientTaskNotFoundException
import de.vinz.openfls.domains.clientTasks.exceptions.InvalidClientTaskException
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.logging.StructuredLog
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
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
    private val entityManager: EntityManager,
    private val clock: Clock
) {

    @Transactional(readOnly = true)
    fun getDtosByClientId(clientId: Long): List<ClientTaskDto> {
        val today = LocalDate.now(clock)
        return clientTaskRepository.findAllByClientIdAndDoneOrderByDueDateAscIdAsc(clientId, false)
            .map { toDto(it, today) }
    }

    @Transactional(readOnly = true)
    fun getCompletedDtosByClientId(clientId: Long, page: Int, size: Int = 10): ClientTaskPageDto {
        if (page < 0 || size !in 1..100) throw InvalidClientTaskException("invalid pagination")
        val result = clientTaskRepository.findAllByClientIdAndDoneOrderByCompletedAtDescIdDesc(
            clientId, true, PageRequest.of(page, size)
        )
        val today = LocalDate.now(clock)
        return ClientTaskPageDto(result.content.map { toDto(it, today) }, result.number, result.size,
            result.totalElements, result.totalPages)
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
    fun getDtoById(id: Long): ClientTaskDto {
        val task = clientTaskRepository.findById(id)
            .orElseThrow { ClientTaskNotFoundException("client task not found") }
        return toDto(task, LocalDate.now(clock))
    }

    @Transactional(readOnly = true)
    fun getAuditHistory(taskId: Long): List<ClientTaskAuditLogDto> {
        return clientTaskAuditLogRepository.findAllByClientTaskIdOrderByChangedAtDesc(taskId)
            .filter { it.action == ClientTaskAuditAction.UPDATE || it.action == ClientTaskAuditAction.COMPLETE }
            .map { toDto(it) }
    }

    @Transactional
    fun create(valueDto: ClientTaskCreateDto, actorId: Long, actorName: String): ClientTaskDto {
        if (valueDto.title.isBlank()) {
            throw InvalidClientTaskException("title must not be blank")
        }

        val now = LocalDateTime.now(clock)
        val task = ClientTask(
            client = entityManager.getReference(Client::class.java, valueDto.clientId),
            title = valueDto.title.trim(),
            description = valueDto.description.trim(),
            dueDate = valueDto.dueDate,
            createdBy = entityManager.getReference(Employee::class.java, actorId),
            createdAt = now,
            done = false
        )

        val saved = clientTaskRepository.save(task)

        writeAuditLog(
            task = saved,
            action = ClientTaskAuditAction.CREATE,
            actorId = actorId,
            actorName = actorName,
            changedAt = now,
            afterTitle = saved.title,
            afterDescription = saved.description,
            afterDueDate = saved.dueDate,
            afterDone = false
        )
        StructuredLog.audit("client.task.created", "success", "client.task", saved.id.toString())

        return toDto(saved, LocalDate.now(clock))
    }

    @Transactional
    fun update(id: Long, valueDto: ClientTaskUpdateDto, actorId: Long, actorName: String): ClientTaskDto {
        val task = clientTaskRepository.findById(id)
            .orElseThrow { ClientTaskNotFoundException("client task not found") }
        if (valueDto.title.isBlank()) {
            throw InvalidClientTaskException("title must not be blank")
        }
        if (task.done) throw InvalidClientTaskException("completed tasks cannot be changed")

        val beforeTitle = task.title
        val beforeDescription = task.description
        val beforeDueDate = task.dueDate

        task.title = valueDto.title.trim()
        task.description = valueDto.description.trim()
        task.dueDate = valueDto.dueDate

        val saved = clientTaskRepository.save(task)
        val now = LocalDateTime.now(clock)

        writeAuditLog(
            task = saved,
            action = ClientTaskAuditAction.UPDATE,
            actorId = actorId,
            actorName = actorName,
            changedAt = now,
            beforeTitle = beforeTitle,
            afterTitle = saved.title,
            beforeDescription = beforeDescription,
            afterDescription = saved.description,
            beforeDueDate = beforeDueDate,
            afterDueDate = saved.dueDate
        )
        StructuredLog.audit("client.task.updated", "success", "client.task", saved.id.toString())

        return toDto(saved, LocalDate.now(clock))
    }

    @Transactional
    fun complete(id: Long, valueDto: CompleteClientTaskDto, actorId: Long, actorName: String): ClientTaskDto {
        val task = clientTaskRepository.findById(id)
            .orElseThrow { ClientTaskNotFoundException("client task not found") }
        if (task.done) {
            throw InvalidClientTaskException("client task is already done")
        }

        val now = LocalDateTime.now(clock)
        task.done = true
        task.completedBy = entityManager.getReference(Employee::class.java, actorId)
        task.completedAt = now
        task.completedOn = valueDto.completedOn
        task.completionComment = valueDto.comment.trim()

        val saved = clientTaskRepository.save(task)

        writeAuditLog(
            task = saved,
            action = ClientTaskAuditAction.COMPLETE,
            actorId = actorId,
            actorName = actorName,
            changedAt = now,
            beforeDone = false,
            afterDone = true,
            comment = saved.completionComment
        )
        StructuredLog.audit("client.task.completed", "success", "client.task", saved.id.toString())

        return toDto(saved, LocalDate.now(clock))
    }

    @Transactional
    fun delete(id: Long, actorId: Long, actorName: String) {
        val task = clientTaskRepository.findById(id)
            .orElseThrow { ClientTaskNotFoundException("client task not found") }

        writeAuditLog(
            task = task,
            action = ClientTaskAuditAction.DELETE,
            actorId = actorId,
            actorName = actorName,
            changedAt = LocalDateTime.now(clock),
            beforeTitle = task.title,
            beforeDescription = task.description,
            beforeDueDate = task.dueDate,
            beforeDone = task.done
        )
        StructuredLog.audit("client.task.deleted", "success", "client.task", task.id.toString())

        clientTaskRepository.delete(task)
    }

    /**
     * Removes every task of a client, e.g. when the client itself is deleted. Each
     * removal is recorded so the audit trail stays complete.
     */
    @Transactional
    fun deleteAllByClientId(clientId: Long, actorId: Long, actorName: String): Int {
        val tasks = clientTaskRepository.findAllByClientId(clientId)
        if (tasks.isEmpty()) {
            return 0
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

        return tasks.size
    }

    fun existsById(id: Long): Boolean = clientTaskRepository.existsById(id)

    private fun writeAuditLog(
        task: ClientTask,
        action: ClientTaskAuditAction,
        actorId: Long,
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

    private fun toDto(task: ClientTask, today: LocalDate): ClientTaskDto {
        return ClientTaskDto(
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

    private fun toDto(log: ClientTaskAuditLog): ClientTaskAuditLogDto {
        return ClientTaskAuditLogDto(
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
