package de.vinz.openfls.domains.evaluations.dto

data class EvaluationMonthResponse(
    val month: Int,
    val assistancePlanActive: Boolean,
    val evaluation: EvaluationResponse?
)
