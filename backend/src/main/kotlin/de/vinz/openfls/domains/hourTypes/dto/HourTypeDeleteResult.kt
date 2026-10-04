package de.vinz.openfls.domains.hourTypes.dto

sealed class HourTypeDeleteResult {
    data class Success(val response: HourTypeResponse) : HourTypeDeleteResult()
    data object NotFound : HourTypeDeleteResult()
}
