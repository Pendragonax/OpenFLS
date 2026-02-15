package de.vinz.openfls.domains.assistancePlans.dtos

import java.time.LocalDate

data class AssistancePlanExistingDto(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val sponsorName: String
)
