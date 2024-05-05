package de.vinz.openfls.domains.contingents.dtos

import java.time.LocalDate
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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