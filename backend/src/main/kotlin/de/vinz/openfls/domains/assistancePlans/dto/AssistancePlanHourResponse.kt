package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour

data class AssistancePlanHourResponse(
    val id: Long = 0,
    val weeklyMinutes: Int = 0,
    val assistancePlanId: Long = 0,
    val hourTypeId: Long = 0
) {
    companion object {
        fun from(hour: AssistancePlanHour): AssistancePlanHourResponse {
            return AssistancePlanHourResponse(
                id = hour.id,
                weeklyMinutes = hour.weeklyMinutes,
                assistancePlanId = hour.assistancePlan?.id ?: 0,
                hourTypeId = hour.hourType?.id ?: 0
            )
        }
    }
}
