package de.vinz.openfls.domains.evaluations.dtos

import de.vinz.openfls.domains.evaluations.dtos.EvaluationMonthResponseDto

class GoalEvaluationsYearDto {
    var goalId: Long = 0

    var title: String = ""

    var months: List<EvaluationMonthResponseDto> = emptyList()
}