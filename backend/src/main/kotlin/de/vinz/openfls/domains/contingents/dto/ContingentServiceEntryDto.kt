package de.vinz.openfls.domains.contingents.dto

import java.time.LocalDateTime

data class ContingentServiceEntryDto(
    val employeeId: Long,
    val start: LocalDateTime,
    val minutes: Int
)
