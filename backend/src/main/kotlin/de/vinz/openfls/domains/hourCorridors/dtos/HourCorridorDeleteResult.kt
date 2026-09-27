package de.vinz.openfls.domains.hourCorridors.dtos

sealed class HourCorridorDeleteResult {
    data class Success(val response: HourCorridorResponse) : HourCorridorDeleteResult()
    data object NotFound : HourCorridorDeleteResult()
    data class Conflict(val assistancePlanCount: Long) : HourCorridorDeleteResult()
}
