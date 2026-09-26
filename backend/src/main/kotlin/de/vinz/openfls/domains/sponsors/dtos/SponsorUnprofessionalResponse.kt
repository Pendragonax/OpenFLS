package de.vinz.openfls.domains.sponsors.dtos

import java.time.LocalDate

data class SponsorUnprofessionalResponse(
    val employeeId: Long,
    val sponsorId: Long,
    val end: LocalDate?
)
