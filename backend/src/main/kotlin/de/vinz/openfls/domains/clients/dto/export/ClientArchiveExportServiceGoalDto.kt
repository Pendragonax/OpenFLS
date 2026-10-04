package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.goals.entity.Goal

data class ClientArchiveExportServiceGoalDto(
    var title: String = "",
    var description: String = ""
) {
    companion object {
        fun from(goal: Goal): ClientArchiveExportServiceGoalDto {
            return ClientArchiveExportServiceGoalDto(
                title = goal.title,
                description = goal.description
            )
        }
    }
}
