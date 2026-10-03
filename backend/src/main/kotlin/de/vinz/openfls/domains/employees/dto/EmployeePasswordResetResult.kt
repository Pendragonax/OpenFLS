package de.vinz.openfls.domains.employees.dto

sealed class EmployeePasswordResetResult {
    data class Success(val response: EmployeeDetailResponse) : EmployeePasswordResetResult()
    data object NotFound : EmployeePasswordResetResult()
}
