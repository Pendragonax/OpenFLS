package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import java.time.LocalDate

data class AssistancePlanResponse(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val clientId: Long,
    val institutionId: Long,
    val institutionName: String,
    val sponsorId: Long,
    val hourMode: AssistancePlanHourMode,
    val hourCorridorId: Long,
    val clientArchived: Boolean
) {
    companion object {
        fun from(plan: AssistancePlan): AssistancePlanResponse {
            return AssistancePlanResponse(
                id = plan.id,
                start = plan.start,
                end = plan.end,
                clientId = plan.client?.id ?: 0,
                institutionId = plan.institution?.id ?: 0,
                institutionName = plan.institution?.name ?: "",
                sponsorId = plan.sponsor?.id ?: 0,
                hourMode = plan.hourMode,
                hourCorridorId = plan.hourCorridor?.id ?: 0,
                clientArchived = plan.client?.archived ?: false
            )
        }
    }
}
