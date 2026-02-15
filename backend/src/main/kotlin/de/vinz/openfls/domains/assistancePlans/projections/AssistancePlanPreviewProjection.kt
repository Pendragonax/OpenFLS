package de.vinz.openfls.domains.assistancePlans.projections

import java.time.LocalDate

interface AssistancePlanPreviewProjection {
    val id: Long
    val clientId: Long
    val institutionId: Long
    val sponsorId: Long
    val clientFirstname: String
    val clientLastname: String
    val institutionName: String
    val sponsorName: String
    val start: LocalDate
    val end: LocalDate
}
