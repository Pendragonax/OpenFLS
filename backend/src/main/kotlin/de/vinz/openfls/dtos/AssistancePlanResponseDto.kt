package de.vinz.openfls.dtos

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

    var goals: MutableSet<GoalResponseDto> = mutableSetOf()

    var hours: MutableSet<AssistancePlanHourResponseDto> = mutableSetOf()
}