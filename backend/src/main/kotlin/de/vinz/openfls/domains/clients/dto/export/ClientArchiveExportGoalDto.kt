package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.goals.entity.Goal

data class ClientArchiveExportGoalDto(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var hours: List<ClientArchiveExportGoalHourDto> = emptyList(),
    var evaluations: List<ClientArchiveExportEvaluationDto> = emptyList()
) {
    companion object {
        fun from(goal: Goal, anonymize: Boolean = false): ClientArchiveExportGoalDto {
            return ClientArchiveExportGoalDto(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                hours = goal.hours.map { ClientArchiveExportGoalHourDto.from(it) },
                evaluations = goal.evaluations.map { ClientArchiveExportEvaluationDto.from(it, anonymize) }
            )
        }
    }
}
