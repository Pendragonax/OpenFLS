package de.vinz.openfls.domains.sponsors.dto

import jakarta.validation.constraints.NotEmpty

data class SponsorCreateRequest(
    @field:NotEmpty
    val name: String = "",
    val payOverhang: Boolean = false,
    val payExact: Boolean = false
)
