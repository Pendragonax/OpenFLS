package de.vinz.openfls.domains.hourTypes.dtos

import de.vinz.openfls.domains.hourTypes.HourType

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
