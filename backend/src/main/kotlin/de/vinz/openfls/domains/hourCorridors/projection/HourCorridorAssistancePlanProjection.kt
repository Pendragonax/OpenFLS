package de.vinz.openfls.domains.hourCorridors.projection

import java.time.LocalDate

interface HourCorridorAssistancePlanProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val clientFirstName: String
    val clientLastName: String
}
