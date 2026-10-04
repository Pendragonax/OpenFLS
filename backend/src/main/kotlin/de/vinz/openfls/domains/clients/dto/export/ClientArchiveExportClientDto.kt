package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.clients.entity.Client

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
