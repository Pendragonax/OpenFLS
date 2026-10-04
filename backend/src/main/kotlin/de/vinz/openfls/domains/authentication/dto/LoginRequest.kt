package de.vinz.openfls.domains.authentication.dto

data class LoginRequest(
    val username: String = "",
    val password: String = ""
)
