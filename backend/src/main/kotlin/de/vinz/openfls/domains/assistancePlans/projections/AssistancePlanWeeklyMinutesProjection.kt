package de.vinz.openfls.domains.assistancePlans.projections

interface AssistancePlanWeeklyMinutesProjection {
    val assistancePlanId: Long
    val weeklyMinutes: Int
}
