package de.vinz.openfls.domains.evaluations.dto

sealed class EvaluationCreateResult {
    data class Success(val response: EvaluationResponse) : EvaluationCreateResult()
    data object GoalNotFound : EvaluationCreateResult()
    data object Forbidden : EvaluationCreateResult()
    data object ClientArchived : EvaluationCreateResult()
}
