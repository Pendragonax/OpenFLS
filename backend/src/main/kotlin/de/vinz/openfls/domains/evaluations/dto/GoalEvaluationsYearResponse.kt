package de.vinz.openfls.domains.evaluations.dto

data class GoalEvaluationsYearResponse(
    val goalId: Long,
    val title: String,
    val months: List<EvaluationMonthResponse>
)
