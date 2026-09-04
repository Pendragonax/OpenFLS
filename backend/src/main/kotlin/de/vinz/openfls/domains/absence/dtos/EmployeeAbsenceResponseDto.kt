package de.vinz.openfls.domains.absence.dtos

import java.time.LocalDate

data class EmployeeAbsenceResponseDTO(
    val employeeId: Long,
    val absenceDates: List<LocalDate>,
)
