package de.vinz.openfls.domains.goals.projections

interface GoalProjection {
    val id: Long
    val title: String
    val description: String
    val hours: List<GoalHourProjection>
}