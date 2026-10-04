package de.vinz.openfls.domains.goals.dto

import de.vinz.openfls.domains.goals.entity.Goal

data class GoalWithHoursResponse(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val assistancePlanId: Long = 0,
    val institutionId: Long? = null,
    val institutionName: String? = null,
    val hours: List<GoalHourResponse> = emptyList()
) {
    companion object {
        fun from(goal: Goal): GoalWithHoursResponse {
            return GoalWithHoursResponse(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                assistancePlanId = goal.assistancePlan?.id ?: 0,
                institutionId = goal.institution?.id,
                institutionName = goal.institution?.name,
                hours = goal.hours.map { GoalHourResponse.from(it) }.sortedBy { it.id }
            )
        }
    }
}
