package de.vinz.openfls.domains.goalTimeEvaluations.dtos

class GoalsTimeEvaluationDto {

    var assistancePlanId: Long = 0

    var goalTimeEvaluations: MutableList<GoalTimeEvaluationDto> = mutableListOf()
}