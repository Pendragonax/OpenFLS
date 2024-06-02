package de.vinz.openfls.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

class GoalDto {
    var id: Long = 0

    @field:NotEmpty(message = "title needed")
    var title: String = ""

    @field:NotEmpty(message = "description needed")
    var description: String = ""

    @field:NotNull(message = "assistanceId needed")
    var assistancePlanId: Long = 0

    var institutionId: Long? = null

    @Valid
    var hours: MutableSet<GoalHourDto> = mutableSetOf()
}