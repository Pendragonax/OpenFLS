package de.vinz.openfls.domains.services.dto

import java.time.LocalDate

data class ClientServicesByDateRequest(
    val clientId: Long,
    val date: LocalDate
)
