package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour

data class AssistancePlanDetailHourResponse(
    val id: Long,
    val weeklyMinutes: Int,
    val hourType: AssistancePlanDetailHourTypeResponse,
    val assistancePlan: AssistancePlanDetailReferenceResponse
) {
    companion object {
        fun from(entity: AssistancePlanHour, plan: AssistancePlan): AssistancePlanDetailHourResponse {
            return AssistancePlanDetailHourResponse(
                id = entity.id,
                weeklyMinutes = entity.weeklyMinutes,
                hourType = AssistancePlanDetailHourTypeResponse.from(entity.hourType),
                assistancePlan = AssistancePlanDetailReferenceResponse(plan.id, plan.start, plan.end)
            )
        }
    }
}
