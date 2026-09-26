package de.vinz.openfls.domains.employees.dtos

import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection

data class EmployeeSoloDto(
    var id: Long = 0,
    var firstname: String = "",
    var lastname: String = "",
    var email: String = "",
    var phonenumber: String = "",
    var description: String = "",
    var archived: Boolean = false
) {
    companion object {
        fun of(projection: EmployeeSoloProjection): EmployeeSoloDto {
            return EmployeeSoloDto(
                id = projection.id,
                firstname = projection.firstname,
                lastname = projection.lastname,
                email = projection.email,
                phonenumber = projection.phonenumber,
                description = projection.description,
                archived = projection.archived
            )
        }
    }
}
