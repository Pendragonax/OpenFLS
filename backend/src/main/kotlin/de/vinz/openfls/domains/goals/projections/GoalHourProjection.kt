package de.vinz.openfls.domains.goals.projections

interface GoalHourProjection {
    val id: Long
    val weeklyMinutes: Int
    val hourType: GoalHourTypeProjection
}
