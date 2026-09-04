package de.vinz.openfls.domains.absence.dtos

import java.time.LocalDate

data class EmployeeAbsenceResponseDto(
    val employeeId: Long,
    val absenceDates: List<LocalDate>,
)
