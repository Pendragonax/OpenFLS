package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.domains.clients.dto.ClientDeleteResult
import de.vinz.openfls.domains.clients.dto.ClientDetailResponse
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.logging.StructuredLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Deleting a client also deletes its assistance plans and documentation, and has to remove what
 * other domains attached to it: the favourites of the employees and the client tasks. Everything is
 * checked before anything is removed, so a refused deletion leaves no cleanup behind.
 */
@Service
class ClientDeletionService(
    private val clientService: ClientService,
    private val employeeService: EmployeeService,
    private val employeeFavoriteService: EmployeeFavoriteService,
    private val clientTaskService: ClientTaskService,
    private val accessService: AccessService
) {

    @Transactional
    fun delete(clientId: Long): ClientDeleteResult {
        val client = clientService.getEntityById(clientId) ?: return ClientDeleteResult.NotFound
        if (client.archived) {
            return ClientDeleteResult.Archived
        }
        val response = ClientDetailResponse.from(client)

        employeeFavoriteService.deleteAssistancePlanFavoritesByClientId(clientId)
        employeeFavoriteService.deleteClientFavoritesByClientId(clientId)
        clientTaskService.deleteAllByClientId(clientId, accessService.getId(), actorName())

        clientService.deleteById(clientId)
        StructuredLog.audit("client.deleted", "success", "client", clientId.toString())

        return ClientDeleteResult.Success(response)
    }

    private fun actorName(): String {
        val employee = employeeService.getEmployeeNameById(accessService.getId(), includeArchived = false)
            ?: return UNKNOWN_ACTOR

        return "${employee.firstName} ${employee.lastName}".trim().ifBlank { UNKNOWN_ACTOR }
    }

    private companion object {
        const val UNKNOWN_ACTOR = "Unbekannt"
    }
}
