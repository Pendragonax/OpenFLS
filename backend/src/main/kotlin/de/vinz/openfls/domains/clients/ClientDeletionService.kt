package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.logging.StructuredLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Deleting a client also has to remove what other domains attached to it: the
 * favourites of the employees and the client tasks. Both are cleaned up before the
 * client itself is removed, so no dangling reference stays behind.
 */
@Service
class ClientDeletionService(
    private val clientService: ClientService,
    private val employeeFavoriteService: EmployeeFavoriteService,
    private val clientTaskService: ClientTaskService
) {

    @Transactional
    fun delete(clientId: Long, actorId: Long, actorName: String) {
        employeeFavoriteService.deleteAssistancePlanFavoritesByClientId(clientId)
        employeeFavoriteService.deleteClientFavoritesByClientId(clientId)
        clientTaskService.deleteAllByClientId(clientId, actorId, actorName)

        clientService.delete(clientId)
        StructuredLog.audit("client.deleted", "success", "client", clientId.toString())
    }
}
