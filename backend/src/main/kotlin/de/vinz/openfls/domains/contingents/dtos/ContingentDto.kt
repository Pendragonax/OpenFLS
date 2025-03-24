package de.vinz.openfls.domains.contingents.dtos

import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.contingents.projections.ContingentProjection
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

    companion object {
        fun from(entity: Contingent): ContingentDto {
            val dto = ContingentDto()
            dto.id = entity.id ?: 0
            dto.start = entity.start
            dto.end = entity.end
            dto.weeklyServiceHours = entity.weeklyServiceHours
            dto.employeeId = entity.employee?.id ?: 0
            dto.institutionId = entity.institution?.id ?: 0
            return dto
        }

        fun from(entity: ContingentProjection): ContingentDto {
            val dto = ContingentDto()
            dto.id = entity.id
            dto.start = entity.start
            dto.end = entity.end
            dto.weeklyServiceHours = entity.weeklyServiceHours
            dto.employeeId = entity.employee.id
            dto.institutionId = entity.institution.id
            return dto
        }
    }
}