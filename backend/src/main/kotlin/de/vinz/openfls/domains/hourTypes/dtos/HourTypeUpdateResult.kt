package de.vinz.openfls.domains.hourTypes.dtos

sealed class HourTypeUpdateResult {
    data class Success(val response: HourTypeResponse) : HourTypeUpdateResult()
    data object NotFound : HourTypeUpdateResult()
}
