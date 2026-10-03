package de.vinz.openfls.domains.assistancePlans.projection

import java.time.LocalDate

interface AssistancePlanExistingProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val sponsorName: String
    val clientArchived: Boolean
}
