package de.vinz.openfls.domains.authentication.dtos

data class AuthenticationResponseDto(
        val userId: Long = 0,
        val token: String = "",
        val expiredAt: String = ""
)
