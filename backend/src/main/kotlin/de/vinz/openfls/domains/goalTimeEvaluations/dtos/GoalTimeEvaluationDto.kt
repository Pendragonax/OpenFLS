package de.vinz.openfls.domains.goalTimeEvaluations.dtos

class GoalTimeEvaluationDto {

    var id: Long = 0

    var title: String = ""

    var description: String = ""

    var executedHours: List<Double> = listOf()

    var summedExecutedHours: List<Double> = listOf()

    var approvedHours: List<Double> = listOf()

    var summedApprovedHours: List<Double> = listOf()

    var approvedHoursLeft: List<Double> = listOf()

    var summedApprovedHoursLeft: List<Double> = listOf()
}