package de.vinz.openfls.domains.employees.dto

import de.vinz.openfls.domains.permissions.dto.PermissionRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

class EmployeeCreateRequest {
    @field:NotEmpty
    var firstName: String = ""

    @field:NotEmpty
    var lastName: String = ""

    var phonenumber: String = ""

    var email: String = ""

    var description: String = ""

    @field:NotNull
    @field:Valid
    var access: EmployeeCreateAccessRequest = EmployeeCreateAccessRequest()

    var permissions: List<PermissionRequest> = listOf()

    var unprofessionals: List<UnprofessionalRequest> = listOf()
}
