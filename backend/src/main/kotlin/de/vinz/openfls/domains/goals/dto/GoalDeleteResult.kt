package de.vinz.openfls.domains.goals.dto

sealed class GoalDeleteResult {
    data class Success(val response: GoalResponse) : GoalDeleteResult()
    data object NotFound : GoalDeleteResult()
}
