package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.goals.dto.GoalWithHoursResponse
import java.time.LocalDate

/** Mutable on purpose: the hour report adjusts the period of its total row. */
class AssistancePlanEditResponse {
    var id: Long = 0

    var start: LocalDate = LocalDate.now()

    var end: LocalDate = LocalDate.now()

    var clientId: Long = 0

    var institutionId: Long = 0

    var institutionName: String = ""

    var sponsorId: Long = 0

    var hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT

    var hourCorridorId: Long = 0

    var clientArchived: Boolean = false

    var goals: List<GoalWithHoursResponse> = listOf()

    var hours: List<AssistancePlanHourResponse> = listOf()

    companion object {
        fun from(plan: AssistancePlan): AssistancePlanEditResponse {
            return AssistancePlanEditResponse().apply {
                id = plan.id
                start = plan.start
                end = plan.end
                clientId = plan.client?.id ?: 0
                institutionId = plan.institution?.id ?: 0
                institutionName = plan.institution?.name ?: ""
                sponsorId = plan.sponsor?.id ?: 0
                hourMode = plan.hourMode
                hourCorridorId = plan.hourCorridor?.id ?: 0
                clientArchived = plan.client?.archived ?: false
                goals = plan.goals.map { GoalWithHoursResponse.from(it) }
                hours = plan.hours.sortedBy { it.id }.map { AssistancePlanHourResponse.from(it) }
            }
        }
    }
}
