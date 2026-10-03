package de.vinz.openfls.domains.services.projection

import java.time.LocalDateTime

interface ContingentEvaluationServiceProjection {
    val id: Long
    val start: LocalDateTime
    val minutes: Int
    val employeeId: Long
}