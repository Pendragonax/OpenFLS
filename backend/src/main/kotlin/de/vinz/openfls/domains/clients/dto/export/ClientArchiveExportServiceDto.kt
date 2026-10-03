package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.services.entity.Service
import java.time.LocalDateTime

data class ClientArchiveExportServiceDto(
    var id: Long = 0,
    var start: LocalDateTime = LocalDateTime.now(),
    var end: LocalDateTime = LocalDateTime.now(),
    var minutes: Int = 0,
    var title: String = "",
    var content: String = "",
    var groupService: Boolean = false,
    var unfinished: Boolean = false,
    var archivedService: Boolean = false,
    var employee: ClientArchiveExportEmployeeDto = ClientArchiveExportEmployeeDto(),
    var institution: ClientArchiveExportInstitutionDto = ClientArchiveExportInstitutionDto(),
    var hourType: ClientArchiveExportHourTypeDto = ClientArchiveExportHourTypeDto(),
    var assistancePlan: ClientArchiveExportAssistancePlanReferenceDto = ClientArchiveExportAssistancePlanReferenceDto(),
    var goals: List<ClientArchiveExportServiceGoalDto> = emptyList(),
    var categories: List<ClientArchiveExportCategoryDto> = emptyList()
) {
    companion object {
        fun from(service: Service, anonymize: Boolean = false): ClientArchiveExportServiceDto {
            return ClientArchiveExportServiceDto(
                id = service.id,
                start = service.start,
                end = service.end,
                minutes = service.minutes,
                title = service.title,
                content = service.content,
                groupService = service.groupService,
                unfinished = service.unfinished,
                archivedService = service.archivedService,
                employee = service.employee?.let { ClientArchiveExportEmployeeDto.from(it, anonymize) } ?: ClientArchiveExportEmployeeDto(),
                institution = service.institution?.let { ClientArchiveExportInstitutionDto.from(it) } ?: ClientArchiveExportInstitutionDto(),
                hourType = service.hourType?.let { ClientArchiveExportHourTypeDto.from(it) } ?: ClientArchiveExportHourTypeDto(),
                assistancePlan = service.assistancePlan?.let { ClientArchiveExportAssistancePlanReferenceDto.from(it) } ?: ClientArchiveExportAssistancePlanReferenceDto(),
                goals = service.goals.map { ClientArchiveExportServiceGoalDto.from(it) },
                categories = service.categorys.map { ClientArchiveExportCategoryDto.from(it) }
            )
        }
    }
}
