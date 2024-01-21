package de.vinz.openfls.domains.evaluations.dtos

import java.time.LocalDate
import java.time.LocalDateTime

class EvaluationResponseDto {
    var id: Long = 0
    var goalId: Long = 0
    var date: LocalDate = LocalDate.now()
    var content: String = ""
    var approved: Boolean = false
    var createdBy: String = ""
    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedBy: String = ""
    var updatedAt: LocalDateTime = LocalDateTime.now()
}