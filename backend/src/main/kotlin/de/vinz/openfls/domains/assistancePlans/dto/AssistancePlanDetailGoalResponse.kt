package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.goals.entity.Goal

data class AssistancePlanDetailGoalResponse(
    val id: Long,
    val title: String,
    val description: String,
    val assistancePlanId: Long,
    val hours: List<AssistancePlanDetailGoalHourResponse>
) {
    companion object {
        fun from(entity: Goal, assistancePlanId: Long): AssistancePlanDetailGoalResponse {
            return AssistancePlanDetailGoalResponse(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                assistancePlanId = assistancePlanId,
                hours = entity.hours
                    .sortedBy { it.id }
                    .map { AssistancePlanDetailGoalHourResponse.from(it) }
            )
        }
    }
}
