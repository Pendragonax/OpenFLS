package de.vinz.openfls.dtos

import jakarta.validation.constraints.NotEmpty

class HourTypeDto {
    var id: Long = 0

    @field:NotEmpty
    var title: String = ""

    var price: Double = 0.0
}