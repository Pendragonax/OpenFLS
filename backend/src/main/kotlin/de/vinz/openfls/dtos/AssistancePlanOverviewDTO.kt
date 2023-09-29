package de.vinz.openfls.dtos

class AssistancePlanOverviewDTO(
        val assistancePlanDto: AssistancePlanDto,
        val clientDto: ClientSimpleDto,
        var values: DoubleArray) {
}