package de.vinz.openfls.domains.hourReports.dto

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanEditResponse
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto

class HourReportRowResponse(
        val assistancePlanDto: AssistancePlanEditResponse,
        val clientDto: ClientSimpleDto,
        var values: DoubleArray)
