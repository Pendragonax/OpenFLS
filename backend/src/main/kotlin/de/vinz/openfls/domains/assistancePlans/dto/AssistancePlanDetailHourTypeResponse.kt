package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.hourTypes.entity.HourType

data class AssistancePlanDetailHourTypeResponse(
    val id: Long,
    val title: String,
    val price: Double
) {
    companion object {
        fun from(entity: HourType?): AssistancePlanDetailHourTypeResponse {
            return AssistancePlanDetailHourTypeResponse(
                id = entity?.id ?: 0,
                title = entity?.title ?: "",
                price = entity?.price ?: 0.0
            )
        }
    }
}
