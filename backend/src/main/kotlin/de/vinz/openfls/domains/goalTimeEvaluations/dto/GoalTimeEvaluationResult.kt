package de.vinz.openfls.domains.goalTimeEvaluations.dto

sealed class GoalTimeEvaluationResult {
    data class Success(val response: GoalsTimeEvaluationResponse) : GoalTimeEvaluationResult()
    data object AssistancePlanNotFound : GoalTimeEvaluationResult()
    data object NoGoalFoundForHourType : GoalTimeEvaluationResult()
}
