package de.vinz.openfls.domains.hourCorridors.projections

import java.time.LocalDate

interface HourCorridorAssistancePlanProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val clientFirstName: String
    val clientLastName: String
}
