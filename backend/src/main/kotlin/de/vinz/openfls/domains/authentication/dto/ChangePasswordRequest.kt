package de.vinz.openfls.domains.authentication.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotEmpty
    @field:Size(min = 6)
    val oldPassword: String = "",

    @field:NotEmpty
    @field:Size(min = 6)
    val newPassword: String = ""
)
