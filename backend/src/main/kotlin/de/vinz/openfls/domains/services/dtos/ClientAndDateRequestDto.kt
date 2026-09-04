package de.vinz.openfls.domains.services.dtos

import java.time.LocalDate

data class ClientAndDateRequestDto(
    val clientId: Long,
    val date: LocalDate) {
}
