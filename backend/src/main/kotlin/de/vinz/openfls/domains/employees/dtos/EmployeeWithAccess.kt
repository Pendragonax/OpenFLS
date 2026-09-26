package de.vinz.openfls.domains.employees.dtos

import de.vinz.openfls.domains.permissions.PermissionDto
import jakarta.validation.Valid

class EmployeeWithAccess {
    var id: Long = 0

    var firstName: String = ""

    var lastName: String = ""

    var phonenumber: String = ""

    var email: String = ""

    var description: String = ""

    var archived: Boolean = false

    var inactive: Boolean = false

    var institutionId: Long? = null

    @Valid
    var access: EmployeeAccessDto? = null

    var permissions: List<PermissionDto>? = null

    var unprofessionals: List<UnprofessionalDto>? = null
}
