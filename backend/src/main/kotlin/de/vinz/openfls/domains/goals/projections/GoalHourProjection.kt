package de.vinz.openfls.domains.goals.projections

import de.vinz.openfls.projections.HourTypeSoloProjection

interface GoalHourProjection {
    val id: Long
    val weeklyHours: Double
    val hourType: HourTypeSoloProjection
}