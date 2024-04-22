package de.vinz.openfls.domains.assistancePlans.projections

import java.time.LocalDate

interface AssistancePlanHourSoloProjection {
    val id: Long
    val weeklyHours: Double
    val end: LocalDate?
}