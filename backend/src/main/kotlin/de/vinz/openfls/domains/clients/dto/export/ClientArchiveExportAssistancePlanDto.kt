package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import java.time.LocalDate

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
