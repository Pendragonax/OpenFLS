package de.vinz.openfls.domains.goals.dto

sealed class GoalCreateResult {
    data class Success(val response: GoalWithHoursResponse) : GoalCreateResult()
    data class AssistancePlanNotFound(val message: String) : GoalCreateResult()
    data class InstitutionNotFound(val message: String) : GoalCreateResult()
    data class HourTypeNotFound(val message: String) : GoalCreateResult()
    data class CorridorHoursNotAllowed(val message: String) : GoalCreateResult()
}
