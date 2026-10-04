package de.vinz.openfls.domains.evaluations.dto

import java.time.LocalDate

data class EvaluationUpdateRequest(
    val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val content: String = "",
    val approved: Boolean = false
)
