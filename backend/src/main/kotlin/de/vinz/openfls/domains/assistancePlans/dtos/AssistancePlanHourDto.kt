package de.vinz.openfls.domains.assistancePlans.dtos

import javax.validation.constraints.Min

class AssistancePlanHourDto {
    var id: Long = 0

    @field:Min(0)
    var weeklyHours: Double = 0.0

    var assistancePlanId: Long = 0

    var hourTypeId: Long = 0
}