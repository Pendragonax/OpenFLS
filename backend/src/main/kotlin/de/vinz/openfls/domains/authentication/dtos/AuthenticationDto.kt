package de.vinz.openfls.domains.authentication.dtos

data class AuthenticationDto(
        val userId: Long = 0,
        val token: String = "",
        val expiredAt: String = ""
)
