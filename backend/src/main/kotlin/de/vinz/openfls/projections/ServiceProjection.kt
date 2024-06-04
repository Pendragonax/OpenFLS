package de.vinz.openfls.projections

import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import java.time.LocalDateTime

interface ServiceProjection {
    val id: Long
    val start: LocalDateTime
    val minutes: Int
    val content: String
    val institution: InstitutionSoloProjection
    val employee: EmployeeSoloProjection
}