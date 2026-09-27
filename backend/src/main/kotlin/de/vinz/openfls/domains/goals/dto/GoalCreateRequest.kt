package de.vinz.openfls.domains.goals.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class GoalCreateRequest(
    @field:NotEmpty(message = "title needed")
    val title: String = "",

    @field:NotEmpty(message = "description needed")
    val description: String = "",

    @field:NotNull(message = "assistanceId needed")
    val assistancePlanId: Long = 0,

    val institutionId: Long? = null,

    @field:Valid
    val hours: List<GoalHourRequest> = emptyList()
)
