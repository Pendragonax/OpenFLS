package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import java.time.LocalDate

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
