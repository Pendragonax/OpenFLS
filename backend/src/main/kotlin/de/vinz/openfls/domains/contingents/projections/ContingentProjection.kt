package de.vinz.openfls.domains.contingents.projections

import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.projections.InstitutionSoloProjection
import java.time.LocalDate

interface ContingentProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate?
    val weeklyServiceHours: Double
    val employee: EmployeeSoloProjection
    val institution: InstitutionSoloProjection
}