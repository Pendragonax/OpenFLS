package de.vinz.openfls.domains.employees.dto

import de.vinz.openfls.domains.employees.entity.Employee

data class EmployeeNameDto(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val archived: Boolean = false
) {
    companion object {
        fun from(employee: Employee): EmployeeNameDto {
            return EmployeeNameDto(
                id = employee.id ?: 0,
                firstName = employee.firstname,
                lastName = employee.lastname,
                archived = employee.archived
            )
        }
    }
}
