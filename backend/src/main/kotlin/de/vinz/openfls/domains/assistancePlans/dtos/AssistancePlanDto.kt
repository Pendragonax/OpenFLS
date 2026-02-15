package de.vinz.openfls.domains.assistancePlans.dtos

import de.vinz.openfls.domains.goals.dtos.GoalDto
import java.time.LocalDate
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

class AssistancePlanDto {
    var id: Long = 0

    //@field:NotNull(message = "start is null")
    var start: LocalDate = LocalDate.now()

    //@field:NotNull(message = "end is null")
    var end: LocalDate = LocalDate.now()

    @field:NotNull(message = "clientId is null")
    var clientId: Long = 0

    @field:NotNull(message = "institutionId is null")
    var institutionId: Long = 0

    var institutionName: String = ""

    @field:NotNull(message = "sponsorId is null")
    var sponsorId: Long = 0

    var goals: MutableSet<GoalDto> = mutableSetOf()

    @Valid
    var hours: MutableSet<AssistancePlanHourDto> = mutableSetOf()
}
