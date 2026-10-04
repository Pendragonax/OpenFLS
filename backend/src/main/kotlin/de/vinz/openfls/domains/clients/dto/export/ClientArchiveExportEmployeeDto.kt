package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.employees.entity.Employee

data class ClientArchiveExportEmployeeDto(
    var id: Long = 0,
    var firstName: String = "",
    var lastName: String = ""
) {
    companion object {
        fun from(employee: Employee, anonymize: Boolean = false): ClientArchiveExportEmployeeDto {
            return ClientArchiveExportEmployeeDto(
                id = employee.id ?: 0,
                firstName = if (anonymize) "Anonym" else employee.firstname,
                lastName = if (anonymize) "Anonym" else employee.lastname
            )
        }
    }
}
