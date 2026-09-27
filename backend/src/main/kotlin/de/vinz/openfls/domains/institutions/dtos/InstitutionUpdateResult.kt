package de.vinz.openfls.domains.institutions.dtos

sealed class InstitutionUpdateResult {
    data class Success(val response: InstitutionWithPermissionsResponse) : InstitutionUpdateResult()
    data object NotFound : InstitutionUpdateResult()
}
