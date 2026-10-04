package de.vinz.openfls.domains.contingents.dto

sealed class ContingentDeleteResult {
    data class Success(val response: ContingentResponse) : ContingentDeleteResult()
    data object NotFound : ContingentDeleteResult()
}
