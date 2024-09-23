package de.vinz.openfls.domains.goalTimeEvaluations.dtos

class GoalsTimeEvaluationDto {

    var assistancePlanId: Long = 0

    var executedHours: List<Double> = listOf()

    var summedExecutedHours: List<Double> = listOf()

    var approvedHours: List<Double> = listOf()

    var summedApprovedHours: List<Double> = listOf()

    var approvedHoursLeft: List<Double> = listOf()

    var summedApprovedHoursLeft: List<Double> = listOf()

    var goalTimeEvaluations: MutableList<GoalTimeEvaluationDto> = mutableListOf()
}