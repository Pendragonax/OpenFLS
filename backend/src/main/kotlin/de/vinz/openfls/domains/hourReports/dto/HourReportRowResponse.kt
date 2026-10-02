package de.vinz.openfls.domains.hourReports.dto

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEditDto
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto

class HourReportRowResponse(
        val assistancePlanDto: AssistancePlanEditDto,
        val clientDto: ClientSimpleDto,
        var values: DoubleArray)
