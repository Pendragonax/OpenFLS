package de.vinz.openfls.projections

import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import java.time.LocalDateTime

interface ServiceWithEmployeeAndInstitutionProjection {
    val id: Long
    val start: LocalDateTime
    val end: LocalDateTime
    val minutes: Int
    val title: String
    val content: String
    val employee: EmployeeSoloProjection
    val institution: InstitutionSoloProjection
}