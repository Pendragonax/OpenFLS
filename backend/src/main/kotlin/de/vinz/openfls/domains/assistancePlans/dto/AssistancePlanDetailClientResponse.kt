package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.clients.entity.Client

data class AssistancePlanDetailClientResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val archived: Boolean
) {
    companion object {
        fun from(client: Client): AssistancePlanDetailClientResponse {
            return AssistancePlanDetailClientResponse(
                id = client.id,
                firstName = client.firstName,
                lastName = client.lastName,
                phoneNumber = client.phoneNumber,
                email = client.email,
                archived = client.archived
            )
        }
    }
}
