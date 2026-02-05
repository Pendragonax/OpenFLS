package de.vinz.openfls.domains.employees.dtos

import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.permissions.PermissionDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

class EmployeeDto {
    var id: Long = 0

    @field:NotEmpty
    var firstName: String = ""

    @field:NotEmpty
    var lastName: String = ""

    var phonenumber: String = ""

    var email: String = ""

    var description: String = ""

    var inactive: Boolean = false

    var institutionId: Long? = null

    @Valid
    var access: EmployeeAccessDto? = null

    var permissions: List<PermissionDto>? = null

    var unprofessionals: List<UnprofessionalDto>? = null

    var contingents: List<ContingentDto>? = null
}