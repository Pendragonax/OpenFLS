package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.goals.dto.GoalWithHoursResponse
import de.vinz.openfls.domains.hourTypes.dto.HourTypeResponse
import java.time.LocalDate

data class AssistancePlanForServiceEditingResponse(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val clientId: Long,
    val institutionId: Long,
    val institutionName: String,
    val sponsorId: Long,
    val hourMode: AssistancePlanHourMode,
    val hourCorridorId: Long,
    val goals: List<GoalWithHoursResponse>,
    val hours: List<AssistancePlanHourResponse>,
    val possibleDocumentationHourTypes: List<HourTypeResponse>
) {
    companion object {
        fun from(plan: AssistancePlan): AssistancePlanForServiceEditingResponse {
            val hourTypes = (plan.hours.mapNotNull { it.hourType } +
                plan.goals.flatMap { goal -> goal.hours.mapNotNull { it.hourType } })
                .distinctBy { it.id }
                .sortedBy { it.title.lowercase() }
                .map { HourTypeResponse.from(it) }

            return AssistancePlanForServiceEditingResponse(
                id = plan.id,
                start = plan.start,
                end = plan.end,
                clientId = plan.client?.id ?: 0,
                institutionId = plan.institution?.id ?: 0,
                institutionName = plan.institution?.name ?: "",
                sponsorId = plan.sponsor?.id ?: 0,
                hourMode = plan.hourMode,
                hourCorridorId = plan.hourCorridor?.id ?: 0,
                goals = plan.goals.map { GoalWithHoursResponse.from(it) },
                hours = plan.hours.sortedBy { it.id }.map { AssistancePlanHourResponse.from(it) },
                possibleDocumentationHourTypes = hourTypes
            )
        }
    }
}
