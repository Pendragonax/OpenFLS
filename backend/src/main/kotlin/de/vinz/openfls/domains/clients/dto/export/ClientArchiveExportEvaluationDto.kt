package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.evaluations.entity.Evaluation
import java.time.LocalDate
import java.time.LocalDateTime

data class ClientArchiveExportEvaluationDto(
    var id: Long = 0,
    var date: LocalDate = LocalDate.now(),
    var content: String = "",
    var approved: Boolean = false,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var createdBy: ClientArchiveExportEmployeeDto = ClientArchiveExportEmployeeDto(),
    var updatedBy: ClientArchiveExportEmployeeDto = ClientArchiveExportEmployeeDto()
) {
    companion object {
        fun from(evaluation: Evaluation, anonymize: Boolean = false): ClientArchiveExportEvaluationDto {
            return ClientArchiveExportEvaluationDto(
                id = evaluation.id,
                date = evaluation.date,
                content = evaluation.content,
                approved = evaluation.approved,
                createdAt = evaluation.createdAt,
                updatedAt = evaluation.updatedAt,
                createdBy = evaluation.createdBy?.let { ClientArchiveExportEmployeeDto.from(it, anonymize) } ?: ClientArchiveExportEmployeeDto(),
                updatedBy = evaluation.updatedBy?.let { ClientArchiveExportEmployeeDto.from(it, anonymize) } ?: ClientArchiveExportEmployeeDto()
            )
        }
    }
}
