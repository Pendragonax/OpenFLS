package de.vinz.openfls.domains.institutions.dto

sealed class InstitutionDeleteResult {
    data class Success(val response: InstitutionWithPermissionsResponse) : InstitutionDeleteResult()
    data object NotFound : InstitutionDeleteResult()
}
