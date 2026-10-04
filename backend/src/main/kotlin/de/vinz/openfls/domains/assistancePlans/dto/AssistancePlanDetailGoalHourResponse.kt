package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.goals.entity.GoalHour

data class AssistancePlanDetailGoalHourResponse(
    val id: Long,
    val weeklyMinutes: Int,
    val hourType: AssistancePlanDetailHourTypeResponse
) {
    companion object {
        fun from(entity: GoalHour): AssistancePlanDetailGoalHourResponse {
            return AssistancePlanDetailGoalHourResponse(
                id = entity.id,
                weeklyMinutes = entity.weeklyMinutes,
                hourType = AssistancePlanDetailHourTypeResponse.from(entity.hourType)
            )
        }
    }
}
