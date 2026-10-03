package de.vinz.openfls.domains.clients.service

import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.domains.clients.dto.ClientDeleteResult
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.employees.dto.EmployeeNameDto
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Client tasks and favourites reference a client by foreign key. They have to be
 * gone before the client itself is removed, and nothing is touched if the deletion is refused.
 */
class ClientDeletionServiceTest {

    private val clientService: ClientService = mock()
    private val employeeService: EmployeeService = mock()
    private val employeeFavoriteService: EmployeeFavoriteService = mock()
    private val clientTaskService: ClientTaskService = mock()
    private val accessService: AccessService = mock()

    private val clientDeletionService = ClientDeletionService(
        clientService,
        employeeService,
        employeeFavoriteService,
        clientTaskService,
        accessService
    )

    @BeforeEach
    fun setUp() {
        whenever(accessService.getId()).thenReturn(7L)
        whenever(employeeService.getEmployeeNameById(7L, includeArchived = false))
            .thenReturn(EmployeeNameDto(id = 7L, firstName = "Anna", lastName = "Autorin"))
    }

    @Test
    fun delete_removesFavoritesAndTasksBeforeTheClient() {
        whenever(clientService.getEntityById(3L)).thenReturn(Client(id = 3L, firstName = "Max", lastName = "Muster"))

        val result = clientDeletionService.delete(3L)

        assertThat(result).isInstanceOf(ClientDeleteResult.Success::class.java)
        inOrder(employeeFavoriteService, clientTaskService, clientService) {
            verify(employeeFavoriteService).deleteAssistancePlanFavoritesByClientId(3)
            verify(employeeFavoriteService).deleteClientFavoritesByClientId(3)
            verify(clientTaskService).deleteAllByClientId(eq(3L), eq(7L), eq("Anna Autorin"))
            verify(clientService).deleteById(3)
        }
    }

    @Test
    fun delete_unknownClient_returnsNotFoundWithoutCleanup() {
        whenever(clientService.getEntityById(3L)).thenReturn(null)

        assertThat(clientDeletionService.delete(3L)).isEqualTo(ClientDeleteResult.NotFound)
        verifyNothingRemoved()
    }

    @Test
    fun delete_archivedClient_returnsArchivedWithoutCleanup() {
        whenever(clientService.getEntityById(3L)).thenReturn(Client(id = 3L, archived = true))

        assertThat(clientDeletionService.delete(3L)).isEqualTo(ClientDeleteResult.Archived)
        verifyNothingRemoved()
    }

    @Test
    fun delete_unknownActor_usesPlaceholderNameForTheTaskAudit() {
        whenever(clientService.getEntityById(3L)).thenReturn(Client(id = 3L))
        whenever(employeeService.getEmployeeNameById(7L, includeArchived = false)).thenReturn(null)

        clientDeletionService.delete(3L)

        verify(clientTaskService).deleteAllByClientId(eq(3L), eq(7L), eq("Unbekannt"))
    }

    private fun verifyNothingRemoved() {
        verify(employeeFavoriteService, never()).deleteAssistancePlanFavoritesByClientId(any())
        verify(employeeFavoriteService, never()).deleteClientFavoritesByClientId(any())
        verify(clientTaskService, never()).deleteAllByClientId(any(), any(), any())
        verify(clientService, never()).deleteById(any())
    }
}
