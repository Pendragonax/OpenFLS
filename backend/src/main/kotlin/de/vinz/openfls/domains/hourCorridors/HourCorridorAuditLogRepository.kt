package de.vinz.openfls.domains.hourCorridors

import org.springframework.data.repository.CrudRepository

interface HourCorridorAuditLogRepository : CrudRepository<HourCorridorAuditLog, Long> {
    fun findAllByHourCorridorIdOrderByChangedAtDescIdDesc(hourCorridorId: Long): List<HourCorridorAuditLog>
}
