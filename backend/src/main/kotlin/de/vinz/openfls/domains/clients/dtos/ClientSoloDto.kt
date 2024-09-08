package de.vinz.openfls.domains.clients.dtos

data class ClientSoloDto(
        var id: Long = 0,
        var firstName: String = "",
        var lastName: String = "",
        var phoneNumber: String = "",
        var email: String = "")