package de.vinz.openfls.domains.clients.dto

import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse

data class ClientDetailResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val archived: Boolean,
    val institution: InstitutionResponse,
    val categoryTemplateId: Long,
    val categoryTemplateTitle: String
) {
    companion object {
        fun from(client: Client): ClientDetailResponse {
            return ClientDetailResponse(
                id = client.id,
                firstName = client.firstName,
                lastName = client.lastName,
                phoneNumber = client.phoneNumber,
                email = client.email,
                archived = client.archived,
                institution = client.institution?.let { InstitutionResponse.from(it) } ?: InstitutionResponse(),
                categoryTemplateId = client.categoryTemplate?.id ?: 0,
                categoryTemplateTitle = client.categoryTemplate?.title ?: ""
            )
        }
    }
}
