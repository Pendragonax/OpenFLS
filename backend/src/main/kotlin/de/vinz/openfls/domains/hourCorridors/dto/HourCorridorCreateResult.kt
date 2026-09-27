package de.vinz.openfls.domains.hourCorridors.dto

sealed class HourCorridorCreateResult {
    data class Success(val response: HourCorridorResponse) : HourCorridorCreateResult()
    data class InvalidRange(val message: String) : HourCorridorCreateResult()
    data class HourTypeNotFound(val message: String) : HourCorridorCreateResult()
}
