package de.vinz.openfls.domains.absence.dto

import java.time.LocalDate

data class AbsenceCreateRequest(
    val absenceDate: LocalDate,
)
