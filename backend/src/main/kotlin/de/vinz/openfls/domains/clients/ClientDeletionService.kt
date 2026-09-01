package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clientTasks.ClientTaskService
import de.vinz.openfls.domains.employees.services.EmployeeService
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
    private val employeeService: EmployeeService,
    private val clientTaskService: ClientTaskService
) {

    @Transactional
    fun delete(clientId: Long, actorId: Long, actorName: String) {
        employeeService.deleteAssistancePlanFavoritesByClientId(clientId)
        employeeService.deleteClientFavoritesByClientId(clientId)
        clientTaskService.deleteAllByClientId(clientId, actorId, actorName)

        clientService.delete(clientId)
        StructuredLog.audit("client.deleted", "success", "client", clientId.toString())
    }
}
