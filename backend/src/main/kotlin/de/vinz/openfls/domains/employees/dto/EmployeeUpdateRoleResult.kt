package de.vinz.openfls.domains.employees.dto

sealed class EmployeeUpdateRoleResult {
    data class Success(val response: EmployeeDetailResponse) : EmployeeUpdateRoleResult()
    data object NotFound : EmployeeUpdateRoleResult()
    data object InvalidRole : EmployeeUpdateRoleResult()
}
