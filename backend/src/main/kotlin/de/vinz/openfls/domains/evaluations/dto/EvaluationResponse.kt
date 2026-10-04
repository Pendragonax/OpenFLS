package de.vinz.openfls.domains.evaluations.dto

import de.vinz.openfls.domains.evaluations.entity.Evaluation
import java.time.LocalDate
import java.time.LocalDateTime

data class EvaluationResponse(
    val id: Long,
    val goalId: Long,
    val date: LocalDate,
    val content: String,
    val approved: Boolean,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedBy: String,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(entity: Evaluation): EvaluationResponse = EvaluationResponse(
            id = entity.id,
            goalId = entity.goal?.id ?: 0,
            date = entity.date,
            content = entity.content,
            approved = entity.approved,
            createdBy = entity.createdBy?.let { "${it.lastname} ${it.firstname}" } ?: "",
            createdAt = entity.createdAt,
            updatedBy = entity.updatedBy?.let { "${it.lastname} ${it.firstname}" } ?: "",
            updatedAt = entity.updatedAt
        )
    }
}
