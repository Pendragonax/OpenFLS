package de.vinz.openfls.dtos

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

class PasswordDto(
    @field:NotEmpty
    @field:Size(min = 6)
    var oldPassword: String,

    @field:NotEmpty
    @field:Size(min = 6)
    var newPassword: String
)