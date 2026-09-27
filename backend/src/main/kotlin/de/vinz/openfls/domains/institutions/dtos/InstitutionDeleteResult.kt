package de.vinz.openfls.domains.institutions.dtos

sealed class InstitutionDeleteResult {
    data class Success(val response: InstitutionWithPermissionsResponse) : InstitutionDeleteResult()
    data object NotFound : InstitutionDeleteResult()
}
