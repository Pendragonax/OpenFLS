package de.vinz.openfls.domains.hourCorridors.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class HourCorridorUpdateRequest(
    @field:PositiveOrZero
    val id: Long = 0,

    @field:NotEmpty
    val title: String = "",

    @field:PositiveOrZero
    val weeklyMinutesFrom: Int = 0,

    @field:PositiveOrZero
    val weeklyMinutesTill: Int = 0,

    @field:Positive
    val hourTypeId: Long = 0
)
