package de.vinz.openfls.domains.employees.dto

sealed class EmployeeUpdateResult {
    data class Success(val response: EmployeeDetailResponse) : EmployeeUpdateResult()
    data object NotFound : EmployeeUpdateResult()
    data object SponsorNotFound : EmployeeUpdateResult()
}
