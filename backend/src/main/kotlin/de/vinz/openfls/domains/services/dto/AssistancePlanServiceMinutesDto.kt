package de.vinz.openfls.domains.services.dto

import de.vinz.openfls.domains.services.projection.AssistancePlanServiceMinutesProjection

data class AssistancePlanServiceMinutesDto(
    val assistancePlanId: Long,
    val minutes: Int
) {
    companion object {
        fun from(projection: AssistancePlanServiceMinutesProjection): AssistancePlanServiceMinutesDto =
            AssistancePlanServiceMinutesDto(projection.assistancePlanId, projection.minutes)
    }
}
