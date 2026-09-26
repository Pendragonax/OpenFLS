package de.vinz.openfls.domains.contingents.dtos

import java.time.LocalDate

data class ContingentCreateRequest(
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate? = null,
    val weeklyServiceHours: Double = 0.0,
    val employeeId: Long = 0,
    val institutionId: Long = 0
)
