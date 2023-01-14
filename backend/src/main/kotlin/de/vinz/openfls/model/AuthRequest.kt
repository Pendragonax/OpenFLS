package de.vinz.openfls.model

@JvmRecord
data class AuthRequest(
    val username: String,

    val password: String)