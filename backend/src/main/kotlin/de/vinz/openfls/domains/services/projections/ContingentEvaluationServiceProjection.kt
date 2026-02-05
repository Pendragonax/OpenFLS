package de.vinz.openfls.domains.services.projections

import java.time.LocalDateTime

interface ContingentEvaluationServiceProjection {
    val id: Long
    val start: LocalDateTime
    val minutes: Int
    val employeeId: Long
}