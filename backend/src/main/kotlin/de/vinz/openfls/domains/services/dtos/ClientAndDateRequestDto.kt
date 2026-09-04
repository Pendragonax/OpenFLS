package de.vinz.openfls.domains.services.dtos

import java.time.LocalDate

data class ClientAndDateRequestDTO(
    val clientId: Long,
    val date: LocalDate) {
}
