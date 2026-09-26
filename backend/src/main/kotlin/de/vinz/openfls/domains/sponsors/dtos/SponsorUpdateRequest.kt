package de.vinz.openfls.domains.sponsors.dtos

import jakarta.validation.constraints.NotEmpty

data class SponsorUpdateRequest(
    val id: Long = 0,
    @field:NotEmpty
    val name: String = "",
    val payOverhang: Boolean = false,
    val payExact: Boolean = false
)
