package de.vinz.openfls.domains.assistancePlans.dtos

class AssistancePlanEvalDto {
    var total: List<ActualTargetValueDto> = listOf()

    var tillToday: List<ActualTargetValueDto> = listOf()

    var actualMonth: List<ActualTargetValueDto> = listOf()

    var actualYear: List<ActualTargetValueDto> = listOf()

    var notMatchingServices: Int = 0

    var notMatchingServicesIds: MutableList<Long> = mutableListOf()
}