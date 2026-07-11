package de.vinz.openfls.domains.hourCorridors.dtos

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class UpdateHourCorridorDto(
    @field:PositiveOrZero
    var id: Long = 0,

    @field:NotEmpty
    var title: String = "",

    @field:PositiveOrZero
    var weeklyMinutesFrom: Int = 0,

    @field:PositiveOrZero
    var weeklyMinutesTill: Int = 0,

    @field:Positive
    var hourTypeId: Long = 0
)
