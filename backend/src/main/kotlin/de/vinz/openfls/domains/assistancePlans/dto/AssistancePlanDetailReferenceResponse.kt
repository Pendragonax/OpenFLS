package de.vinz.openfls.domains.assistancePlans.dto

import java.time.LocalDate

data class AssistancePlanDetailReferenceResponse(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate
)
