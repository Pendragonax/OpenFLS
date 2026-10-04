package de.vinz.openfls.domains.hourCorridors.dto

import de.vinz.openfls.domains.hourCorridors.HourCorridorAuditAction
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridorAuditLog
import java.time.LocalDateTime

data class HourCorridorAuditLogResponse(
    val id: Long,
    val hourCorridorId: Long,
    val action: HourCorridorAuditAction,
    val changedAt: LocalDateTime,
    val actor: String,
    val beforeTitle: String?,
    val afterTitle: String?,
    val beforeWeeklyMinutesFrom: Int?,
    val afterWeeklyMinutesFrom: Int?,
    val beforeWeeklyMinutesTill: Int?,
    val afterWeeklyMinutesTill: Int?,
    val beforeHourTypeId: Long?,
    val afterHourTypeId: Long?
) {
    companion object {
        fun from(log: HourCorridorAuditLog) = HourCorridorAuditLogResponse(
            log.id, log.hourCorridorId, log.action, log.changedAt, log.actor,
            log.beforeTitle, log.afterTitle,
            log.beforeWeeklyMinutesFrom, log.afterWeeklyMinutesFrom,
            log.beforeWeeklyMinutesTill, log.afterWeeklyMinutesTill,
            log.beforeHourTypeId, log.afterHourTypeId
        )
    }
}
