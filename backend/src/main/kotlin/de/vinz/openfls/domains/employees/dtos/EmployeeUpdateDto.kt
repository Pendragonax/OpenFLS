package de.vinz.openfls.domains.employees.dtos

import de.vinz.openfls.domains.permissions.dto.PermissionRequest
import jakarta.validation.constraints.NotEmpty

class EmployeeUpdateDto {
    var id: Long = 0

    @field:NotEmpty
    var firstName: String = ""

    @field:NotEmpty
    var lastName: String = ""

    var phonenumber: String = ""

    var email: String = ""

    var description: String = ""

    var permissions: List<PermissionRequest> = listOf()

    var unprofessionals: List<UnprofessionalDto> = listOf()
}
