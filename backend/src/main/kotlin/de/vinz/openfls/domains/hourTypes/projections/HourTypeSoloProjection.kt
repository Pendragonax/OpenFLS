package de.vinz.openfls.domains.hourTypes.projections

import de.vinz.openfls.domains.hourTypes.HourType

interface HourTypeSoloProjection {
    val id: Long
    val title: String
    val price: Double

    companion object {
        fun from(value: HourType?): HourTypeSoloProjection {
            return object : HourTypeSoloProjection {
                override val id = value?.id ?: 0
                override val title = value?.title ?: ""
                override val price = value?.price ?: 0.0
            }
        }
    }
}