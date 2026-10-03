package de.vinz.openfls.domains.assistancePlans.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

class AssistancePlanCreateGoalRequest {
    @field:NotEmpty(message = "title needed")
    var title: String = ""

    var description: String = ""

    var institutionId: Long? = null

    @field:Valid
    var hours: List<AssistancePlanCreateHourRequest> = listOf()
}
