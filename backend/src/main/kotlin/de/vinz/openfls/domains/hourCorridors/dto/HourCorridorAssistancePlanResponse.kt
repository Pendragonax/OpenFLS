package de.vinz.openfls.domains.hourCorridors.dto

import de.vinz.openfls.domains.hourCorridors.projection.HourCorridorAssistancePlanProjection
import java.time.LocalDate

data class HourCorridorAssistancePlanResponse(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val clientFirstName: String,
    val clientLastName: String
) {
    companion object {
        fun from(p: HourCorridorAssistancePlanProjection) = HourCorridorAssistancePlanResponse(p.id, p.start, p.end, p.clientFirstName, p.clientLastName)
    }
}
