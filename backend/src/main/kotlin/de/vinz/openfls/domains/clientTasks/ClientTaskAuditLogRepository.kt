package de.vinz.openfls.domains.clientTasks

import org.springframework.data.repository.CrudRepository

interface ClientTaskAuditLogRepository : CrudRepository<ClientTaskAuditLog, Long> {
    fun findAllByClientTaskIdOrderByChangedAtDesc(clientTaskId: Long): List<ClientTaskAuditLog>
}
