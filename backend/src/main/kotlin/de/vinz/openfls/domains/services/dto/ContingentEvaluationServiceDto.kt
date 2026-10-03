package de.vinz.openfls.domains.services.dto

import de.vinz.openfls.domains.services.projection.ContingentEvaluationServiceProjection
import java.time.LocalDateTime

data class ContingentEvaluationServiceDto(
    val id: Long,
    val start: LocalDateTime,
    val minutes: Int,
    val employeeId: Long
) {
    companion object {
        fun from(projection: ContingentEvaluationServiceProjection): ContingentEvaluationServiceDto =
            ContingentEvaluationServiceDto(projection.id, projection.start, projection.minutes, projection.employeeId)
    }
}
