package de.vinz.openfls.domains.services.projection

import java.time.LocalDateTime

interface ServiceTimeSlotProjection {
    val id: Long
    val start: LocalDateTime
    val end: LocalDateTime
    val employeeFirstname: String
    val employeeLastname: String
}
