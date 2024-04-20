package de.vinz.openfls.projections

import java.time.LocalDate

interface AssistancePlanProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val institution: InstitutionSoloProjection
    val hours: List<AssistancePlanHourSoloProjection>
}