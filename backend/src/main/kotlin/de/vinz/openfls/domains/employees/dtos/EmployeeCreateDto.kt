package de.vinz.openfls.domains.employees.dtos

import de.vinz.openfls.domains.permissions.PermissionDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

class EmployeeCreateDto {
    @field:NotEmpty
    var firstName: String = ""

    @field:NotEmpty
    var lastName: String = ""

    var phonenumber: String = ""

    var email: String = ""

    var description: String = ""

    var institutionId: Long? = null

    @field:NotNull
    @field:Valid
    var access: EmployeeAccessDto = EmployeeAccessDto()

    var permissions: List<PermissionDto> = listOf()

    var unprofessionals: List<UnprofessionalDto> = listOf()
}
