package de.vinz.openfls.domains.clients.dto

import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse

data class ClientWithInstitutionResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val archived: Boolean,
    val institution: InstitutionResponse
) {
    companion object {
        fun from(client: Client): ClientWithInstitutionResponse {
            return ClientWithInstitutionResponse(
                id = client.id,
                firstName = client.firstName,
                lastName = client.lastName,
                phoneNumber = client.phoneNumber,
                email = client.email,
                archived = client.archived,
                institution = client.institution?.let { InstitutionResponse.from(it) } ?: InstitutionResponse()
            )
        }
    }
}
