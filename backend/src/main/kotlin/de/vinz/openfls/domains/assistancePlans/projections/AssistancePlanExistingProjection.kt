package de.vinz.openfls.domains.assistancePlans.projections

import java.time.LocalDate

interface AssistancePlanExistingProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val sponsorName: String
}
