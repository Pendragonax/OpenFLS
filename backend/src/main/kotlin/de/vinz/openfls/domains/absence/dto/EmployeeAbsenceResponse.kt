package de.vinz.openfls.domains.absence.dto

import java.time.LocalDate

data class EmployeeAbsenceResponse(
    val employeeId: Long,
    val absenceDates: List<LocalDate>,
)
