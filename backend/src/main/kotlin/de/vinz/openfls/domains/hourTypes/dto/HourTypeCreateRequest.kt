package de.vinz.openfls.domains.hourTypes.dto

import jakarta.validation.constraints.NotEmpty

data class HourTypeCreateRequest(
    @field:NotEmpty
    val title: String = "",
    val price: Double = 0.0
)
