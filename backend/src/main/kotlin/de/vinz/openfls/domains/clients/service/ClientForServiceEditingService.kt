package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.dto.ClientForServiceEditingResponse
import de.vinz.openfls.domains.permissions.service.AccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * The client together with the assistance plans of the institutions the signed-in employee may
 * document in, as the form for new and edited services needs it.
 */
@Service
class ClientForServiceEditingService(
    private val clientService: ClientService,
    private val assistancePlanService: AssistancePlanService,
    private val accessService: AccessService
) {

    @Transactional(readOnly = true)
    fun getForServiceEditingById(clientId: Long): ClientForServiceEditingResponse? {
        val client = clientService.getEntityById(clientId) ?: return null
        val visible = !client.archived ||
            accessService.isAdmin() ||
            accessService.getLeadingInstitutionIds().contains(client.institution?.id ?: 0)
        if (!visible) {
            return null
        }

        val writableInstitutionIds = accessService.getWriteRightsInstitutionIds(accessService.getId())

        return ClientForServiceEditingResponse.from(
            client,
            assistancePlanService.getAllForServiceEditingByClientId(clientId, writableInstitutionIds)
        )
    }
}
