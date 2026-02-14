package de.vinz.openfls.domains.assistancePlans.dtos

import jakarta.validation.constraints.Min

class AssistancePlanHourDto {
    var id: Long = 0

    @field:Min(0)
    var weeklyMinutes: Int = 0

    var assistancePlanId: Long = 0

    var hourTypeId: Long = 0
}
