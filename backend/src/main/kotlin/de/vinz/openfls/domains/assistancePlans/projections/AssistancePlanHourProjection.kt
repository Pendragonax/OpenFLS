package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.hourTypes.HourTypeSoloProjection
import java.time.LocalDate

interface AssistancePlanHourProjection {
    val id: Long
    val weeklyHours: Double
    val end: LocalDate?
    val hourType: HourTypeSoloProjection
}