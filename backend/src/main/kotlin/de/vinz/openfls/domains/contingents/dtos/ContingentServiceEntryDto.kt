package de.vinz.openfls.domains.contingents.dtos

import java.time.LocalDateTime

data class ContingentServiceEntryDto(
    val employeeId: Long,
    val start: LocalDateTime,
    val minutes: Int
)
