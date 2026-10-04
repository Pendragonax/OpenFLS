package de.vinz.openfls.domains.evaluations.dto

sealed class EvaluationYearResult {
    data class Success(val response: EvaluationYearResponse) : EvaluationYearResult()
    data object AssistancePlanNotFound : EvaluationYearResult()
    data object Forbidden : EvaluationYearResult()
}
