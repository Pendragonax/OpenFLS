package de.vinz.openfls.domains.assistancePlans.projections

import java.time.LocalDate

interface AssistancePlanPreviewProjection {
    val id: Long
    val clientFirstname: String
    val clientLastname: String
    val institutionName: String
    val sponsorName: String
    val start: LocalDate
    val end: LocalDate
}
