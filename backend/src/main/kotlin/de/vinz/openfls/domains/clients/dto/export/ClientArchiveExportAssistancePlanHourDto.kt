package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour

data class ClientArchiveExportAssistancePlanHourDto(
    var id: Long = 0,
    var weeklyMinutes: Int = 0,
    var hourType: ClientArchiveExportHourTypeDto = ClientArchiveExportHourTypeDto()
) {
    companion object {
        fun from(hour: AssistancePlanHour): ClientArchiveExportAssistancePlanHourDto {
            return ClientArchiveExportAssistancePlanHourDto(
                id = hour.id,
                weeklyMinutes = hour.weeklyMinutes,
                hourType = hour.hourType?.let { ClientArchiveExportHourTypeDto.from(it) } ?: ClientArchiveExportHourTypeDto()
            )
        }
    }
}
