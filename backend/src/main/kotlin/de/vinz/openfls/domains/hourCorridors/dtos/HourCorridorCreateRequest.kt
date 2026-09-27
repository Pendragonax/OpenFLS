package de.vinz.openfls.domains.hourCorridors.dtos

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class HourCorridorCreateRequest(
    @field:NotEmpty
    val title: String = "",

    @field:PositiveOrZero
    val weeklyMinutesFrom: Int = 0,

    @field:PositiveOrZero
    val weeklyMinutesTill: Int = 0,

    @field:Positive
    val hourTypeId: Long = 0
)
