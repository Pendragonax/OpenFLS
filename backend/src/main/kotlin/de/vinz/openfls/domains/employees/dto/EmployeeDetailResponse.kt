package de.vinz.openfls.domains.employees.dto

import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.permissions.dto.PermissionResponse

data class EmployeeDetailResponse(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val phonenumber: String = "",
    val email: String = "",
    val description: String = "",
    val archived: Boolean = false,
    val inactive: Boolean = false,
    val access: EmployeeAccessResponse? = null,
    val permissions: List<PermissionResponse> = listOf(),
    val unprofessionals: List<UnprofessionalResponse> = listOf()
) {
    companion object {
        fun from(employee: Employee): EmployeeDetailResponse {
            return EmployeeDetailResponse(
                id = employee.id ?: 0,
                firstName = employee.firstname,
                lastName = employee.lastname,
                phonenumber = employee.phonenumber,
                email = employee.email,
                description = employee.description,
                archived = employee.archived,
                inactive = employee.inactive,
                access = employee.access?.let { EmployeeAccessResponse.from(it) },
                permissions = employee.permissions.orEmpty().map { PermissionResponse.from(it) },
                unprofessionals = employee.unprofessionals.orEmpty().map { UnprofessionalResponse.from(it) }
            )
        }
    }
}
