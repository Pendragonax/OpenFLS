package de.vinz.openfls.domains.evaluations.dto

data class EvaluationYearResponse(
    val year: Int,
    val values: List<GoalEvaluationsYearResponse>
)
