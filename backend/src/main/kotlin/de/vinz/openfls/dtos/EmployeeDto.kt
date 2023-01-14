package de.vinz.openfls.dtos

import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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

    var permissions: Array<PermissionDto>? = null

    var unprofessionals: Array<UnprofessionalDto>? = null

    var contingents: Array<ContingentDto>? = null
}