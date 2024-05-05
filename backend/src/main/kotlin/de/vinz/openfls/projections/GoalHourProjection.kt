package de.vinz.openfls.projections

interface GoalHourProjection {
    val id: Long
    val weeklyHours: Double
    val hourType: HourTypeSoloProjection
}