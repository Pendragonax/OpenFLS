package de.vinz.openfls.domains.employees.dto

import java.time.LocalDate

data class UnprofessionalRequest(
    var sponsorId: Long = 0,
    var end: LocalDate? = null
)
