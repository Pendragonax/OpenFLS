package de.vinz.openfls.domains.assistancePlans.dto

import jakarta.validation.constraints.Min

class AssistancePlanCreateHourRequest {
    @field:Min(0)
    var weeklyMinutes: Int = 0

    var hourTypeId: Long = 0
}
