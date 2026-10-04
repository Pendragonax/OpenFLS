package de.vinz.openfls.domains.hourCorridors.repository

import de.vinz.openfls.domains.hourCorridors.entity.HourCorridorAuditLog
import org.springframework.data.repository.CrudRepository

interface HourCorridorAuditLogRepository : CrudRepository<HourCorridorAuditLog, Long> {
    fun findAllByHourCorridorIdOrderByChangedAtDescIdDesc(hourCorridorId: Long): List<HourCorridorAuditLog>
}
