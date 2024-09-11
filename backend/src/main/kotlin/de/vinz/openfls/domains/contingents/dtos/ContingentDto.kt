package de.vinz.openfls.domains.contingents.dtos

import jakarta.validation.constraints.NotNull
import java.time.LocalDate

class ContingentDto {
    var id: Long = 0

    @field:NotNull
    var start: LocalDate = LocalDate.now()

    var end: LocalDate? = null

    @field:NotNull
    var weeklyServiceHours: Double = 0.0

    @field:NotNull
    var employeeId: Long = 0

    @field:NotNull
    var institutionId: Long = 0
}