package de.vinz.openfls.domains.services.dtos

import de.vinz.openfls.domains.services.projections.ServiceProjection
import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection
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
            (source.institution as InstitutionSoloProjection?)?.let { InstitutionDto(it.id, it.name, it.email, it.phonenumber) }
                ?: InstitutionDto(0, "", "", ""),
            (source.employee as EmployeeSoloProjection?)?.let { EmployeeDto(it.id, it.firstname, it.lastname, it.email, it.phonenumber, it.description, it.archived) }
                ?: EmployeeDto(0, "", "", "", "", "", false),
            (source.client as ClientSoloProjection?)?.let { ClientDto(it.id, it.firstName, it.lastName, it.phoneNumber, it.email, it.archived) }
                ?: ClientDto(0, "", "", "", "", false)
        )
    }
}
