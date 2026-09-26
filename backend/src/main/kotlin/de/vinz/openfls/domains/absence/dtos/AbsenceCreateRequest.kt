package de.vinz.openfls.domains.absence.dtos

import java.time.LocalDate

data class AbsenceCreateRequest(
    val absenceDate: LocalDate,
)
