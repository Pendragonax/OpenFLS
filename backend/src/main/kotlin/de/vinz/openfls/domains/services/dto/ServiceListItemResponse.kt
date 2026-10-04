package de.vinz.openfls.domains.services.dto

import de.vinz.openfls.domains.services.projection.ServiceClientProjection
import de.vinz.openfls.domains.services.projection.ServiceEmployeeProjection
import de.vinz.openfls.domains.services.projection.ServiceInstitutionProjection
import de.vinz.openfls.domains.services.projection.ServiceWithRelationsProjection
import java.time.LocalDateTime

data class ServiceListItemResponse(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val minutes: Int,
    val title: String,
    val content: String,
    val groupService: Boolean,
    val archivedService: Boolean,
    val institution: InstitutionSummary,
    val employee: EmployeeSummary,
    val client: ClientSummary
) {
    data class InstitutionSummary(val id: Long, val name: String, val email: String, val phonenumber: String)
    data class EmployeeSummary(val id: Long, val firstname: String, val lastname: String, val email: String, val phonenumber: String, val description: String, val archived: Boolean)
    data class ClientSummary(val id: Long, val firstName: String, val lastName: String, val phoneNumber: String, val email: String, val archived: Boolean)

    companion object {
        fun from(source: ServiceWithRelationsProjection) = ServiceListItemResponse(
            source.id, source.start, source.end, source.minutes, source.title, source.content,
            source.groupService, source.archivedService,
            (source.institution as ServiceInstitutionProjection?)?.let { InstitutionSummary(it.id, it.name, it.email, it.phonenumber) }
                ?: InstitutionSummary(0, "", "", ""),
            (source.employee as ServiceEmployeeProjection?)?.let { EmployeeSummary(it.id, it.firstname, it.lastname, it.email, it.phonenumber, it.description, it.archived) }
                ?: EmployeeSummary(0, "", "", "", "", "", false),
            (source.client as ServiceClientProjection?)?.let { ClientSummary(it.id, it.firstName, it.lastName, it.phoneNumber, it.email, it.archived) }
                ?: ClientSummary(0, "", "", "", "", false)
        )
    }
}
