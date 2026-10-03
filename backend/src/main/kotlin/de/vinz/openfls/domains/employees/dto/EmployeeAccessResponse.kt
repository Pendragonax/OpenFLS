package de.vinz.openfls.domains.employees.dto

import de.vinz.openfls.domains.employees.entity.EmployeeAccess

data class EmployeeAccessResponse(
    val id: Long = 0,
    val username: String = "",
    val role: Int = 3
) {
    companion object {
        fun from(access: EmployeeAccess): EmployeeAccessResponse {
            return EmployeeAccessResponse(
                id = access.id ?: 0,
                username = access.username,
                role = access.role
            )
        }
    }
}
