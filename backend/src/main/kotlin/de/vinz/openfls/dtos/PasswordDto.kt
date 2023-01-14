package de.vinz.openfls.dtos

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

class PasswordDto(
    @field:NotEmpty
    @field:Size(min = 6)
    var oldPassword: String,

    @field:NotEmpty
    @field:Size(min = 6)
    var newPassword: String
)