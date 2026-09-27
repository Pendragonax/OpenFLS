package de.vinz.openfls.domains.assistancePlans.dtos

import de.vinz.openfls.domains.goals.dto.GoalWithHoursResponse
import java.time.LocalDate

class AssistancePlanResponseDto {
    var id: Long = 0

    var start: LocalDate = LocalDate.now()

    var end: LocalDate = LocalDate.now()

    var clientId: Long = 0

    var clientFirstName: String = ""

    var clientLastName: String = ""

    var institutionId: Long = 0

    var institutionName: String = ""

    var sponsorId: Long = 0

    var sponsorName: String = ""

    var goals: MutableSet<GoalWithHoursResponse> = mutableSetOf()

    var hours: MutableSet<AssistancePlanHourResponseDto> = mutableSetOf()
}