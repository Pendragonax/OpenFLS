package de.vinz.openfls.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto

class AssistancePlanOverviewDTO(
        val assistancePlanDto: AssistancePlanDto,
        val clientDto: ClientSimpleDto,
        var values: DoubleArray) {
}