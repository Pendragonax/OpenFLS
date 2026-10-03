package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

class AssistancePlanUpdateRequest {
    var id: Long = 0

    @field:NotNull(message = "start is null")
    var start: LocalDate = LocalDate.now()

    @field:NotNull(message = "end is null")
    var end: LocalDate = LocalDate.now()

    @field:NotNull(message = "clientId is null")
    var clientId: Long = 0

    @field:NotNull(message = "institutionId is null")
    var institutionId: Long = 0

    @field:NotNull(message = "sponsorId is null")
    var sponsorId: Long = 0

    var hourMode: AssistancePlanHourMode = AssistancePlanHourMode.EXACT

    var hourCorridorId: Long = 0

    @field:Valid
    var hours: List<AssistancePlanUpdateHourRequest> = listOf()

    @field:Valid
    var goals: List<AssistancePlanUpdateGoalRequest> = listOf()
}
