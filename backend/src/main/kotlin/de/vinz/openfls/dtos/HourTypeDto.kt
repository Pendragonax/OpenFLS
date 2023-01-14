package de.vinz.openfls.dtos

import javax.validation.constraints.NotEmpty

class HourTypeDto {
    var id: Long = 0

    @field:NotEmpty
    var title: String = ""

    var price: Double = 0.0
}