package de.vinz.openfls.domains.clients.dto

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanForServiceEditingResponse
import de.vinz.openfls.domains.categories.dto.CategoryTemplateWithCategoriesResponse
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse

data class ClientForServiceEditingResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val archived: Boolean,
    val categoryTemplate: CategoryTemplateWithCategoriesResponse,
    val institution: InstitutionResponse,
    val assistancePlans: List<AssistancePlanForServiceEditingResponse>
) {
    companion object {
        fun from(
            client: Client,
            assistancePlans: List<AssistancePlanForServiceEditingResponse>
        ): ClientForServiceEditingResponse {
            val categoryTemplate = client.categoryTemplate?.let { CategoryTemplateWithCategoriesResponse.from(it) }
                ?: CategoryTemplateWithCategoriesResponse()

            return ClientForServiceEditingResponse(
                id = client.id,
                firstName = client.firstName,
                lastName = client.lastName,
                phoneNumber = client.phoneNumber,
                email = client.email,
                archived = client.archived,
                categoryTemplate = categoryTemplate.copy(categories = categoryTemplate.categories.sortedBy { it.shortcut }),
                institution = client.institution?.let { InstitutionResponse.from(it) } ?: InstitutionResponse(),
                assistancePlans = assistancePlans
            )
        }
    }
}
