package de.vinz.openfls.dtos

import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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