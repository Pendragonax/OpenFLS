package de.vinz.openfls.domains.assistancePlans.projections

interface AssistancePlanHourCorridorProjection {
    val id: Long
    val title: String
    val weeklyMinutesFrom: Int
    val weeklyMinutesTill: Int
    val hourType: AssistancePlanHourTypeProjection?
}
