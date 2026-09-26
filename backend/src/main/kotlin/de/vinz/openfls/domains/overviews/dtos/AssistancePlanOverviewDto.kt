package de.vinz.openfls.domains.overviews.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEditDto
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto

class AssistancePlanOverviewDto(
        val assistancePlanDto: AssistancePlanEditDto,
        val clientDto: ClientSimpleDto,
        var values: DoubleArray)
