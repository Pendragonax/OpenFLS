package de.vinz.openfls.domains.employees.archive

data class EmployeeArchiveActor(
    val employeeId: Long,
    val firstname: String,
    val lastname: String,
    val isAdmin: Boolean
)
