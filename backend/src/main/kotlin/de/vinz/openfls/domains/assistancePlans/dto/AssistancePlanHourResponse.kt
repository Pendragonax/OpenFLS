package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour

/** Mutable on purpose: [AssistancePlanForServiceEditingDto] is still filled by ModelMapper. */
class AssistancePlanHourResponse {
    var id: Long = 0

    var weeklyMinutes: Int = 0

    var assistancePlanId: Long = 0

    var hourTypeId: Long = 0

    companion object {
        fun from(hour: AssistancePlanHour): AssistancePlanHourResponse {
            return AssistancePlanHourResponse().apply {
                id = hour.id
                weeklyMinutes = hour.weeklyMinutes
                assistancePlanId = hour.assistancePlan?.id ?: 0
                hourTypeId = hour.hourType?.id ?: 0
            }
        }
    }
}
