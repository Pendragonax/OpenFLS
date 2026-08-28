package de.vinz.openfls.domains.hourCorridors.dtos

import de.vinz.openfls.domains.hourCorridors.HourCorridorAuditAction
import de.vinz.openfls.domains.hourCorridors.HourCorridorAuditLog
import java.time.LocalDateTime

data class HourCorridorAuditLogDto(
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
        fun from(log: HourCorridorAuditLog) = HourCorridorAuditLogDto(
            log.id, log.hourCorridorId, log.action, log.changedAt, log.actor,
            log.beforeTitle, log.afterTitle,
            log.beforeWeeklyMinutesFrom, log.afterWeeklyMinutesFrom,
            log.beforeWeeklyMinutesTill, log.afterWeeklyMinutesTill,
            log.beforeHourTypeId, log.afterHourTypeId
        )
    }
}
