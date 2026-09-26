package de.vinz.openfls.domains.assistancePlans.dtos

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.goals.dtos.GoalWithHours
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeResponse
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
    var goals: MutableSet<GoalWithHours> = mutableSetOf()
    var hours: MutableSet<AssistancePlanHourDto> = mutableSetOf()
    var possibleDocumentationHourTypes: Array<HourTypeResponse> = emptyArray()
}
