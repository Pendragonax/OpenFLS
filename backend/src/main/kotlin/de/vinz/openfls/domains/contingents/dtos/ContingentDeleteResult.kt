package de.vinz.openfls.domains.contingents.dtos

sealed class ContingentDeleteResult {
    data class Success(val response: ContingentResponse) : ContingentDeleteResult()
    data object NotFound : ContingentDeleteResult()
}
