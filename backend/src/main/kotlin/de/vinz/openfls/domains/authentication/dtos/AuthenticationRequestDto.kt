package de.vinz.openfls.domains.authentication.dtos

data class AuthenticationRequestDto(
        val username: String,
        val password: String
)