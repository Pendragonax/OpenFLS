package de.vinz.openfls.domains.assistancePlans.projections

interface AssistancePlanGoalHourProjection {
    val id: Long
    val weeklyMinutes: Int
    val hourType: AssistancePlanHourTypeProjection
}
