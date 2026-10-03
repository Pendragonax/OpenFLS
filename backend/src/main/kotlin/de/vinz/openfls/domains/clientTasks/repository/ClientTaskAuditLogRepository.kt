package de.vinz.openfls.domains.clientTasks.repository

import de.vinz.openfls.domains.clientTasks.entity.ClientTaskAuditLog
import org.springframework.data.repository.CrudRepository

interface ClientTaskAuditLogRepository : CrudRepository<ClientTaskAuditLog, Long> {
    fun findAllByClientTaskIdOrderByChangedAtDesc(clientTaskId: Long): List<ClientTaskAuditLog>
}
