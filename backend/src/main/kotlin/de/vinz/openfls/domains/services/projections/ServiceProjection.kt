package de.vinz.openfls.domains.services.projections

import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection
import java.time.LocalDateTime

interface ServiceProjection {
    val id: Long
    val start: LocalDateTime
    val minutes: Int
    val title: String
    val content: String
    val groupService: Boolean
    val institution: InstitutionSoloProjection
    val employee: EmployeeSoloProjection
    val client: ClientSoloProjection
}