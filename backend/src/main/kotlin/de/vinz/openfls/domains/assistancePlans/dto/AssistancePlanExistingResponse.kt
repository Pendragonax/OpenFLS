package de.vinz.openfls.domains.assistancePlans.dto

import java.time.LocalDate

data class AssistancePlanExistingResponse(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val sponsorName: String,
    val clientArchived: Boolean
)
