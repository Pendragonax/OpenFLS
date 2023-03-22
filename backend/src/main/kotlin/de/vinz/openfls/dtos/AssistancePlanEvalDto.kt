package de.vinz.openfls.dtos

class AssistancePlanEvalDto {
    var total: ActualTargetValueDto = ActualTargetValueDto()

    var months: List<ActualTargetValueDateDto> = listOf()
}