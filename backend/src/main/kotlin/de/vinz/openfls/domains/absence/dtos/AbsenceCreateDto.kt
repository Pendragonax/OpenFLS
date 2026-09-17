package de.vinz.openfls.domains.absence.dtos

import java.time.LocalDate

data class AbsenceCreateDto(
    val employeeId: Long,
    val absenceDate: LocalDate,
)