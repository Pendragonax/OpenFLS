package de.vinz.openfls.domains.hourTypes

import jakarta.validation.constraints.NotEmpty

data class HourTypeDto(
    var id: Long = 0,
    @field:NotEmpty
    var title: String = "",
    var price: Double = 0.0) {

    companion object {
        fun from(hourType: HourType): HourTypeDto {
            return HourTypeDto(
                    id = hourType.id,
                    title = hourType.title,
                    price = hourType.price)
        }
    }
}