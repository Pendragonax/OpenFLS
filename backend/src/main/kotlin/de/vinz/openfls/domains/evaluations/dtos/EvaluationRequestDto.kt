package de.vinz.openfls.domains.evaluations.dtos

import java.time.LocalDate

class EvaluationRequestDto {
    var id: Long = 0
    var goalId: Long = 0
    var date: LocalDate = LocalDate.now()
    var content: String = ""
    var approved: Boolean = false
}