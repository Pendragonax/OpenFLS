package de.vinz.openfls.domains.contingents.dto

import java.time.LocalDate

data class ContingentUpdateRequest(
    val id: Long = 0,
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate? = null,
    val weeklyServiceHours: Double = 0.0,
    val employeeId: Long = 0,
    val institutionId: Long = 0
)
