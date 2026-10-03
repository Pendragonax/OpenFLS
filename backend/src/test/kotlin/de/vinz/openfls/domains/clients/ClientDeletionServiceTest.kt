package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.domains.employees.services.EmployeeService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Client tasks and favourites reference a client by foreign key. They have to be
 * gone before the client itself is removed.
 */
class ClientDeletionServiceTest {

    private val clientService: ClientService = mock()
    private val employeeService: EmployeeService = mock()
    private val clientTaskService: ClientTaskService = mock()

    private val clientDeletionService = ClientDeletionService(clientService, employeeService, clientTaskService)

    @Test
    fun delete_removesFavoritesAndTasksBeforeTheClient() {
        clientDeletionService.delete(clientId = 3, actorId = 7, actorName = "Anna Autorin")

        inOrder(employeeService, clientTaskService, clientService) {
            verify(employeeService).deleteAssistancePlanFavoritesByClientId(3)
            verify(employeeService).deleteClientFavoritesByClientId(3)
            verify(clientTaskService).deleteAllByClientId(eq(3L), eq(7L), eq("Anna Autorin"))
            verify(clientService).delete(3)
        }
    }
}
