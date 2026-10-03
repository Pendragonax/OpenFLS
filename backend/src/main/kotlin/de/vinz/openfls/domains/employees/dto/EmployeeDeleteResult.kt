package de.vinz.openfls.domains.employees.dto

sealed class EmployeeDeleteResult {
    data class Success(val response: EmployeeDetailResponse) : EmployeeDeleteResult()
    data object NotFound : EmployeeDeleteResult()
    data object HasServices : EmployeeDeleteResult()
}
