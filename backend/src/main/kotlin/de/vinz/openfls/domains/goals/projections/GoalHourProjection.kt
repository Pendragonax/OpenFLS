package de.vinz.openfls.domains.goals.projections

import de.vinz.openfls.domains.hourTypes.projections.HourTypeSoloProjection

interface GoalHourProjection {
    val id: Long
    val weeklyMinutes: Int
    val hourType: HourTypeSoloProjection
}
