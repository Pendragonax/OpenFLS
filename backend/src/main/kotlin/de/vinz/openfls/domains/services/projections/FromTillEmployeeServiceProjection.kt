package de.vinz.openfls.domains.services.projections

import java.time.LocalDateTime

interface FromTillEmployeeServiceProjection {
    val id: Long
    val start: LocalDateTime
    val end: LocalDateTime
    val employeeFirstname: String
    val employeeLastname: String
}
