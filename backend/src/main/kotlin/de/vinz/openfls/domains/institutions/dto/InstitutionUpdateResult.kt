package de.vinz.openfls.domains.institutions.dto

sealed class InstitutionUpdateResult {
    data class Success(val response: InstitutionWithPermissionsResponse) : InstitutionUpdateResult()
    data object NotFound : InstitutionUpdateResult()
    data object EmployeeNotFound : InstitutionUpdateResult()
}
