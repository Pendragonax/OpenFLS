package de.vinz.openfls.domains.clients.dto

import de.vinz.openfls.domains.clients.entity.Client

/** Id and name of a client, for reports that list clients without any further data. */
data class ClientNameDto(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = ""
) {
    companion object {
        fun from(client: Client): ClientNameDto {
            return ClientNameDto(id = client.id, firstName = client.firstName, lastName = client.lastName)
        }
    }
}
