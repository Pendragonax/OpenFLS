package de.vinz.openfls.domains.employees.dtos

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

class EmployeeAccessDto {
    var id: Long = 0

    @field:NotEmpty
    @field:Size(min = 6)
    var username: String = ""

    var password: String = ""

    var role: Int = 3
}