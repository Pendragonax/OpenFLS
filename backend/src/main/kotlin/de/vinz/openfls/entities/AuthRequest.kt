package de.vinz.openfls.entities

@JvmRecord
data class AuthRequest(
    val username: String,

    val password: String)