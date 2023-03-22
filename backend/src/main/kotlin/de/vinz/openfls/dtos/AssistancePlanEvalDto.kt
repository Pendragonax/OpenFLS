package de.vinz.openfls.dtos

class AssistancePlanEvalDto {
    var total: List<ActualTargetValueDto> = listOf()

    var tillToday: List<ActualTargetValueDto> = listOf()

    var notMatchingServices: Int = 0

    var notMatchingServicesIds: MutableList<Long> = mutableListOf()
}