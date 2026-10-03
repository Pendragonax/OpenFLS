package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor

data class AssistancePlanDetailHourCorridorResponse(
    val id: Long,
    val title: String,
    val weeklyMinutesFrom: Int,
    val weeklyMinutesTill: Int,
    val hourTypeId: Long,
    val hourTypeTitle: String
) {
    companion object {
        fun from(entity: HourCorridor): AssistancePlanDetailHourCorridorResponse {
            return AssistancePlanDetailHourCorridorResponse(
                id = entity.id,
                title = entity.title,
                weeklyMinutesFrom = entity.weeklyMinutesFrom,
                weeklyMinutesTill = entity.weeklyMinutesTill,
                hourTypeId = entity.hourType?.id ?: 0,
                hourTypeTitle = entity.hourType?.title ?: ""
            )
        }
    }
}
