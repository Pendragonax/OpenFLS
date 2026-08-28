package de.vinz.openfls.domains.hourCorridors.dtos

import de.vinz.openfls.domains.hourCorridors.projections.HourCorridorAssistancePlanProjection
import java.time.LocalDate

data class HourCorridorAssistancePlanDto(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val clientFirstName: String,
    val clientLastName: String
) {
    companion object {
        fun from(p: HourCorridorAssistancePlanProjection) = HourCorridorAssistancePlanDto(p.id, p.start, p.end, p.clientFirstName, p.clientLastName)
    }
}
