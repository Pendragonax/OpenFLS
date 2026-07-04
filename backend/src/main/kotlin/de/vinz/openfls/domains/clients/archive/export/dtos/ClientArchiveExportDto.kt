package de.vinz.openfls.domains.clients.archive.export.dtos

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.evaluations.Evaluation
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.sponsors.Sponsor
import java.time.LocalDate
import java.time.LocalDateTime

data class ClientArchiveExportDto(
    var client: ClientArchiveExportClientDto = ClientArchiveExportClientDto(),
    var services: List<ClientArchiveExportServiceDto> = emptyList(),
    var assistancePlans: List<ClientArchiveExportAssistancePlanDto> = emptyList()
) {
    companion object {
        fun from(
            client: Client,
            services: List<Service>,
            assistancePlans: List<AssistancePlan>,
            anonymize: Boolean = false
        ): ClientArchiveExportDto {
            return ClientArchiveExportDto(
                client = ClientArchiveExportClientDto.from(client),
                services = services.map { ClientArchiveExportServiceDto.from(it, anonymize) },
                assistancePlans = assistancePlans.map { ClientArchiveExportAssistancePlanDto.from(it, anonymize) }
            )
        }
    }
}

data class ClientArchiveExportClientDto(
    var id: Long = 0,
    var firstName: String = "",
    var lastName: String = "",
    var archived: Boolean = false
) {
    companion object {
        fun from(client: Client): ClientArchiveExportClientDto {
            return ClientArchiveExportClientDto(
                id = client.id,
                firstName = client.firstName,
                lastName = client.lastName,
                archived = client.archived
            )
        }
    }
}

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

data class ClientArchiveExportEmployeeDto(
    var id: Long = 0,
    var firstName: String = "",
    var lastName: String = ""
) {
    companion object {
        fun from(employee: Employee, anonymize: Boolean = false): ClientArchiveExportEmployeeDto {
            return ClientArchiveExportEmployeeDto(
                id = employee.id ?: 0,
                firstName = if (anonymize) "Anonym" else employee.firstname,
                lastName = if (anonymize) "Anonym" else employee.lastname
            )
        }
    }
}

data class ClientArchiveExportInstitutionDto(
    var id: Long = 0,
    var name: String = ""
) {
    companion object {
        fun from(institution: Institution): ClientArchiveExportInstitutionDto {
            return ClientArchiveExportInstitutionDto(
                id = institution.id ?: 0,
                name = institution.name
            )
        }
    }
}

data class ClientArchiveExportSponsorDto(
    var id: Long = 0,
    var name: String = ""
) {
    companion object {
        fun from(sponsor: Sponsor): ClientArchiveExportSponsorDto {
            return ClientArchiveExportSponsorDto(
                id = sponsor.id,
                name = sponsor.name
            )
        }
    }
}

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

data class ClientArchiveExportCategoryDto(
    var id: Long = 0,
    var title: String = "",
    var shortcut: String = ""
) {
    companion object {
        fun from(category: Category): ClientArchiveExportCategoryDto {
            return ClientArchiveExportCategoryDto(
                id = category.id,
                title = category.title,
                shortcut = category.shortcut
            )
        }
    }
}

data class ClientArchiveExportAssistancePlanHourDto(
    var id: Long = 0,
    var weeklyMinutes: Int = 0,
    var hourType: ClientArchiveExportHourTypeDto = ClientArchiveExportHourTypeDto()
) {
    companion object {
        fun from(hour: de.vinz.openfls.domains.assistancePlans.AssistancePlanHour): ClientArchiveExportAssistancePlanHourDto {
            return ClientArchiveExportAssistancePlanHourDto(
                id = hour.id,
                weeklyMinutes = hour.weeklyMinutes,
                hourType = hour.hourType?.let { ClientArchiveExportHourTypeDto.from(it) } ?: ClientArchiveExportHourTypeDto()
            )
        }
    }
}

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

data class ClientArchiveExportAssistancePlanDto(
    var id: Long = 0,
    var start: LocalDate = LocalDate.now(),
    var end: LocalDate = LocalDate.now(),
    var sponsor: ClientArchiveExportSponsorDto = ClientArchiveExportSponsorDto(),
    var institution: ClientArchiveExportInstitutionDto = ClientArchiveExportInstitutionDto(),
    var hours: List<ClientArchiveExportAssistancePlanHourDto> = emptyList(),
    var goals: List<ClientArchiveExportGoalDto> = emptyList()
) {
    companion object {
        fun from(assistancePlan: AssistancePlan, anonymize: Boolean = false): ClientArchiveExportAssistancePlanDto {
            return ClientArchiveExportAssistancePlanDto(
                id = assistancePlan.id,
                start = assistancePlan.start,
                end = assistancePlan.end,
                sponsor = assistancePlan.sponsor?.let { ClientArchiveExportSponsorDto.from(it) } ?: ClientArchiveExportSponsorDto(),
                institution = assistancePlan.institution?.let { ClientArchiveExportInstitutionDto.from(it) } ?: ClientArchiveExportInstitutionDto(),
                hours = assistancePlan.hours.map { ClientArchiveExportAssistancePlanHourDto.from(it) },
                goals = assistancePlan.goals.map { ClientArchiveExportGoalDto.from(it, anonymize) }
            )
        }
    }
}

data class ClientArchiveExportAssistancePlanReferenceDto(
    var id: Long = 0,
    var start: LocalDate = LocalDate.now(),
    var end: LocalDate = LocalDate.now()
) {
    companion object {
        fun from(assistancePlan: AssistancePlan): ClientArchiveExportAssistancePlanReferenceDto {
            return ClientArchiveExportAssistancePlanReferenceDto(
                id = assistancePlan.id,
                start = assistancePlan.start,
                end = assistancePlan.end
            )
        }
    }
}
