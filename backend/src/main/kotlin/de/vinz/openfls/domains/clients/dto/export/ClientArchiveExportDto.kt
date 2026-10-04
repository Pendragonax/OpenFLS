package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.services.entity.Service

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
