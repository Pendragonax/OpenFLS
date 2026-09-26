package de.vinz.openfls.domains.assistancePlans.projections

interface AssistancePlanHourProjection {
    val id: Long
    val weeklyMinutes: Int
    val hourType: AssistancePlanHourTypeProjection
    val assistancePlan: AssistancePlanSoloProjection

}
