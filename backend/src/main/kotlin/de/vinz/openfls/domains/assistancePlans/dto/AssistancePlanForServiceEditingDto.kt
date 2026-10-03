package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.goals.dto.GoalWithHoursResponse
import de.vinz.openfls.domains.hourTypes.dto.HourTypeResponse
import java.time.LocalDate

class AssistancePlanForServiceEditingDto {
    var id: Long = 0
    var start: LocalDate = LocalDate.now()
    var end: LocalDate = LocalDate.now()
    var clientId: Long = 0
    var institutionId: Long = 0
    var institutionName: String = ""
    var sponsorId: Long = 0
    var hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT
    var hourCorridorId: Long = 0
    var goals: MutableSet<GoalWithHoursResponse> = mutableSetOf()
    var hours: MutableSet<AssistancePlanHourResponse> = mutableSetOf()
    var possibleDocumentationHourTypes: Array<HourTypeResponse> = emptyArray()
}
