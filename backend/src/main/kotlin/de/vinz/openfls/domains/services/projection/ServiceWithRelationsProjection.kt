package de.vinz.openfls.domains.services.projection

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
    val employee: ServiceEmployeeProjection
    val client: ServiceClientProjection
}
