package de.vinz.openfls.dtos

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

class EmployeeAccessDto {
    var id: Long = 0

    @field:NotEmpty
    @field:Size(min = 6)
    var username: String = ""

    var password: String = ""

    var role: Int = 3
}