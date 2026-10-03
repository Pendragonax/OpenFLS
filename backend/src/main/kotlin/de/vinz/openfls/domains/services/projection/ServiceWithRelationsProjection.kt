package de.vinz.openfls.domains.services.projection

import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.domains.services.projection.ServiceInstitutionProjection
import java.time.LocalDateTime

interface ServiceWithRelationsProjection {
    val id: Long
    val start: LocalDateTime
    val end: LocalDateTime
    val minutes: Int
    val title: String
    val content: String
    val groupService: Boolean
    val archivedService: Boolean
    val institution: ServiceInstitutionProjection
    val employee: EmployeeSoloProjection
    val client: ClientSoloProjection
}
