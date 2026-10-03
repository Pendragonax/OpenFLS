package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.goals.entity.GoalHour

data class ClientArchiveExportGoalHourDto(
    var id: Long = 0,
    var weeklyMinutes: Int = 0,
    var hourType: ClientArchiveExportHourTypeDto = ClientArchiveExportHourTypeDto()
) {
    companion object {
        fun from(hour: GoalHour): ClientArchiveExportGoalHourDto {
            return ClientArchiveExportGoalHourDto(
                id = hour.id,
                weeklyMinutes = hour.weeklyMinutes,
                hourType = hour.hourType?.let { ClientArchiveExportHourTypeDto.from(it) } ?: ClientArchiveExportHourTypeDto()
            )
        }
    }
}
