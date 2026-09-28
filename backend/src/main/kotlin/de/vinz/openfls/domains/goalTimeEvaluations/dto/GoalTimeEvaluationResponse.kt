package de.vinz.openfls.domains.goalTimeEvaluations.dto

data class GoalTimeEvaluationResponse(
    val id: Long,
    val title: String,
    val description: String,
    val executedHours: List<Double>,
    val summedExecutedHours: List<Double>,
    val approvedHours: List<Double>,
    val summedApprovedHours: List<Double>,
    val approvedHoursLeft: List<Double>,
    val summedApprovedHoursLeft: List<Double>
)
