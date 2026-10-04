package de.vinz.openfls.domains.goals.dto

import de.vinz.openfls.domains.goals.entity.GoalHour

data class GoalHourResponse(
    val id: Long = 0,
    val weeklyMinutes: Int = 0,
    val hourTypeId: Long = 0,
    val hourTypeTitle: String = ""
) {
    companion object {
        fun from(goalHour: GoalHour): GoalHourResponse {
            return GoalHourResponse(
                id = goalHour.id,
                weeklyMinutes = goalHour.weeklyMinutes,
                hourTypeId = goalHour.hourType?.id ?: 0,
                hourTypeTitle = goalHour.hourType?.title ?: ""
            )
        }
    }
}
