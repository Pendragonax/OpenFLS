package de.vinz.openfls.domains.employees.dto

sealed class EmployeeCreateResult {
    data class Success(val response: EmployeeDetailResponse) : EmployeeCreateResult()
    data class InvalidUsername(val reason: String) : EmployeeCreateResult()
    data object UsernameTaken : EmployeeCreateResult()
    data object InvalidRole : EmployeeCreateResult()
    data object SponsorNotFound : EmployeeCreateResult()
}
