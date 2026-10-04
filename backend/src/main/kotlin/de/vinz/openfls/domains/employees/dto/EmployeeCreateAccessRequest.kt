package de.vinz.openfls.domains.employees.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

class EmployeeCreateAccessRequest {
    @field:NotEmpty
    @field:Size(min = 6)
    var username: String = ""

    var role: Int = 3
}
