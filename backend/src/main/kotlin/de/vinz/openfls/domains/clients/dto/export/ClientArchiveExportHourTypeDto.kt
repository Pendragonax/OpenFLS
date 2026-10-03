package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.hourTypes.entity.HourType

data class ClientArchiveExportHourTypeDto(
    var id: Long = 0,
    var title: String = ""
) {
    companion object {
        fun from(hourType: HourType): ClientArchiveExportHourTypeDto {
            return ClientArchiveExportHourTypeDto(
                id = hourType.id,
                title = hourType.title
            )
        }
    }
}
