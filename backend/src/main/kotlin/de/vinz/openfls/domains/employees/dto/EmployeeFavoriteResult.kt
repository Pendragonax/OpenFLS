package de.vinz.openfls.domains.employees.dto

sealed class EmployeeFavoriteResult {
    data object Success : EmployeeFavoriteResult()
    data object EmployeeNotFound : EmployeeFavoriteResult()
    data object AssistancePlanNotFound : EmployeeFavoriteResult()
    data object ClientNotFound : EmployeeFavoriteResult()
}
