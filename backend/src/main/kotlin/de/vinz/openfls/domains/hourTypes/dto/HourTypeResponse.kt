package de.vinz.openfls.domains.hourTypes.dto

import de.vinz.openfls.domains.hourTypes.entity.HourType

data class HourTypeResponse(
    val id: Long = 0,
    val title: String = "",
    val price: Double = 0.0
) {
    companion object {
        fun from(hourType: HourType): HourTypeResponse {
            return HourTypeResponse(
                id = hourType.id,
                title = hourType.title,
                price = hourType.price
            )
        }
    }
}
