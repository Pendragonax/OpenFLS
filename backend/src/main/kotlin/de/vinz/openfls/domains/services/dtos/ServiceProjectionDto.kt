package de.vinz.openfls.domains.services.dtos

import de.vinz.openfls.domains.services.projections.ServiceProjection
import java.time.LocalDateTime

data class ServiceProjectionDto(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val minutes: Int,
    val title: String,
    val content: String,
    val groupService: Boolean,
    val archivedService: Boolean,
    val institution: InstitutionDto,
    val employee: EmployeeDto,
    val client: ClientDto
) {
    data class InstitutionDto(val id: Long, val name: String, val email: String, val phonenumber: String)
    data class EmployeeDto(val id: Long, val firstname: String, val lastname: String, val email: String, val phonenumber: String, val description: String, val archived: Boolean)
    data class ClientDto(val id: Long, val firstName: String, val lastName: String, val phoneNumber: String, val email: String, val archived: Boolean)

    companion object {
        fun from(source: ServiceProjection) = ServiceProjectionDto(
            source.id, source.start, source.end, source.minutes, source.title, source.content,
            source.groupService, source.archivedService,
            InstitutionDto(source.institution.id, source.institution.name, source.institution.email, source.institution.phonenumber),
            EmployeeDto(source.employee.id, source.employee.firstname, source.employee.lastname, source.employee.email, source.employee.phonenumber, source.employee.description, source.employee.archived),
            ClientDto(source.client.id, source.client.firstName, source.client.lastName, source.client.phoneNumber, source.client.email, source.client.archived)
        )
    }
}
