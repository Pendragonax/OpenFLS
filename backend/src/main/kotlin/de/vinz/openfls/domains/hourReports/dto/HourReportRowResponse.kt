package de.vinz.openfls.domains.hourReports.dto

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanEditResponse
import de.vinz.openfls.domains.clients.dto.ClientNameDto

class HourReportRowResponse(
        val assistancePlanDto: AssistancePlanEditResponse,
        val clientDto: ClientNameDto,
        var values: DoubleArray)
