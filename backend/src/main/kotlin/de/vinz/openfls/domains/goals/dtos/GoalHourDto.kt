package de.vinz.openfls.domains.goals.dtos

import jakarta.validation.constraints.Min

class GoalHourDto {
    var id: Long = 0

    @field:Min(0)
    var weeklyHours: Double = 0.0

    var goalId: Long = 0

    var hourTypeId: Long = 0
}