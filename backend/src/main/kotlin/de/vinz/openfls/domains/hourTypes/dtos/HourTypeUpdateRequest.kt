package de.vinz.openfls.domains.hourTypes.dtos

import jakarta.validation.constraints.NotEmpty

data class HourTypeUpdateRequest(
    val id: Long = 0,
    @field:NotEmpty
    val title: String = "",
    val price: Double = 0.0
)
