package de.vinz.openfls.domains.evaluations.dto

sealed class EvaluationDeleteResult {
    data class Success(val response: EvaluationResponse) : EvaluationDeleteResult()
    data object NotFound : EvaluationDeleteResult()
    data object Forbidden : EvaluationDeleteResult()
    data object ClientArchived : EvaluationDeleteResult()
}
