package de.vinz.openfls.domains.assistancePlans.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

class AssistancePlanCreateDto {
    @field:NotNull(message = "start is null")
    var start: LocalDate = LocalDate.now()

    @field:NotNull(message = "end is null")
    var end: LocalDate = LocalDate.now()

    @field:NotNull(message = "clientId is null")
    var clientId: Long = 0

    @field:NotNull(message = "institutionId is null")
    var institutionId: Long = 0

    @field:NotNull(message = "sponsorId is null")
    var sponsorId: Long = 0

    @field:Valid
    var hours: List<AssistancePlanCreateHourDto> = listOf()

    @field:Valid
    var goals: List<AssistancePlanCreateGoalDto> = listOf()
}

class AssistancePlanCreateHourDto {
    var weeklyMinutes: Int = 0
    var hourTypeId: Long = 0
}

class AssistancePlanCreateGoalDto {
    var title: String = ""
    var description: String = ""
    var assistancePlanId: Long = 0
    var institutionId: Long? = null

    @field:Valid
    var hours: List<AssistancePlanCreateHourDto> = listOf()
}

