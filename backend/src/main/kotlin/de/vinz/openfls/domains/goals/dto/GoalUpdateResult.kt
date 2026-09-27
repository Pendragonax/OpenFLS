package de.vinz.openfls.domains.goals.dto

sealed class GoalUpdateResult {
    data class Success(val response: GoalWithHoursResponse) : GoalUpdateResult()
    data object NotFound : GoalUpdateResult()
    data class AssistancePlanNotFound(val message: String) : GoalUpdateResult()
    data class InstitutionNotFound(val message: String) : GoalUpdateResult()
    data class HourTypeNotFound(val message: String) : GoalUpdateResult()
    data class HourNotInGoal(val message: String) : GoalUpdateResult()
    data class CorridorHoursNotAllowed(val message: String) : GoalUpdateResult()
}
