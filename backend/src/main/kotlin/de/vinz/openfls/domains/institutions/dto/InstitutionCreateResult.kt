package de.vinz.openfls.domains.institutions.dto

sealed class InstitutionCreateResult {
    data class Success(val response: InstitutionWithPermissionsResponse) : InstitutionCreateResult()
    data object EmployeeNotFound : InstitutionCreateResult()
}
