package de.vinz.openfls.domains.evaluations.dto

sealed class EvaluationUpdateResult {
    data class Success(val response: EvaluationResponse) : EvaluationUpdateResult()
    data object NotFound : EvaluationUpdateResult()
    data object Forbidden : EvaluationUpdateResult()
    data object ClientArchived : EvaluationUpdateResult()
}
