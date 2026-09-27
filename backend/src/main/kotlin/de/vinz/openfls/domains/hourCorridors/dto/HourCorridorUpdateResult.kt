package de.vinz.openfls.domains.hourCorridors.dto

sealed class HourCorridorUpdateResult {
    data class Success(val response: HourCorridorResponse) : HourCorridorUpdateResult()
    data object NotFound : HourCorridorUpdateResult()
    data class InvalidRange(val message: String) : HourCorridorUpdateResult()
    data class HourTypeNotFound(val message: String) : HourCorridorUpdateResult()
}
