package de.vinz.openfls.domains.authentication.dto

data class LoginResponse(
    val userId: Long = 0,
    val token: String = "",
    val expiredAt: String = ""
)
