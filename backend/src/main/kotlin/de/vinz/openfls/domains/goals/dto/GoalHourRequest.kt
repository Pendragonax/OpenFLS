package de.vinz.openfls.domains.goals.dto

import jakarta.validation.constraints.Min

data class GoalHourRequest(
    val id: Long = 0,
    @field:Min(0)
    val weeklyMinutes: Int = 0,
    val hourTypeId: Long = 0
)
