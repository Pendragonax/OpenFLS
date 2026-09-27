package de.vinz.openfls.domains.assistancePlans.projections

interface AssistancePlanGoalProjection {
    val id: Long
    val title: String
    val description: String
    val hours: List<AssistancePlanGoalHourProjection>
}
