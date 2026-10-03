package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.employees.dto.EmployeeFavoriteResult
import de.vinz.openfls.domains.employees.entity.Employee
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EmployeeFavoriteServiceTest {

    private val employeeService: EmployeeService = mock()
    private val assistancePlanService: AssistancePlanService = mock()
    private val clientService: ClientService = mock()
    private val employeeFavoriteService = EmployeeFavoriteService(employeeService, assistancePlanService, clientService)

    @Test
    fun addClientFavorite_unknownEmployee_returnsEmployeeNotFound() {
        assertThat(employeeFavoriteService.addClientFavorite(1L, 2L)).isEqualTo(EmployeeFavoriteResult.EmployeeNotFound)
    }

    @Test
    fun addClientFavorite_unknownClient_returnsClientNotFound() {
        // Given
        whenever(employeeService.getEntityById(1L)).thenReturn(Employee(id = 1L))

        // When / Then
        assertThat(employeeFavoriteService.addClientFavorite(1L, 2L)).isEqualTo(EmployeeFavoriteResult.ClientNotFound)
    }

    @Test
    fun addClientFavorite_newFavorite_isStoredOnce() {
        // Given
        val employee = Employee(id = 1L)
        whenever(employeeService.getEntityById(1L)).thenReturn(employee)
        whenever(clientService.getEntityById(2L)).thenReturn(Client(id = 2L))

        // When
        val first = employeeFavoriteService.addClientFavorite(1L, 2L)
        val second = employeeFavoriteService.addClientFavorite(1L, 2L)

        // Then
        assertThat(first).isEqualTo(EmployeeFavoriteResult.Success)
        assertThat(second).isEqualTo(EmployeeFavoriteResult.Success)
        assertThat(employee.clientFavorites.map { it.id }).containsExactly(2L)
        verify(employeeService).saveEntity(employee)
    }

    @Test
    fun deleteClientFavorite_existingFavorite_isRemoved() {
        // Given
        val employee = Employee(id = 1L, clientFavorites = mutableSetOf(Client(id = 2L), Client(id = 3L)))
        whenever(employeeService.getEntityById(1L)).thenReturn(employee)

        // When
        val result = employeeFavoriteService.deleteClientFavorite(1L, 2L)

        // Then
        assertThat(result).isEqualTo(EmployeeFavoriteResult.Success)
        assertThat(employee.clientFavorites.map { it.id }).containsExactly(3L)
    }

    @Test
    fun deleteClientFavorite_noFavorite_doesNotSave() {
        // Given
        val employee = Employee(id = 1L)
        whenever(employeeService.getEntityById(1L)).thenReturn(employee)

        // When
        employeeFavoriteService.deleteClientFavorite(1L, 2L)

        // Then
        verify(employeeService, never()).saveEntity(employee)
    }

    @Test
    fun addAssistancePlanFavorite_unknownAssistancePlan_returnsAssistancePlanNotFound() {
        // Given
        whenever(employeeService.getEntityById(1L)).thenReturn(Employee(id = 1L))

        // When / Then
        assertThat(employeeFavoriteService.addAssistancePlanFavorite(1L, 5L))
            .isEqualTo(EmployeeFavoriteResult.AssistancePlanNotFound)
    }

    @Test
    fun addAssistancePlanFavorite_andDelete_roundTrip() {
        // Given
        val employee = Employee(id = 1L)
        whenever(employeeService.getEntityById(1L)).thenReturn(employee)
        whenever(assistancePlanService.getEntityById(5L)).thenReturn(AssistancePlan(id = 5L))

        // When / Then
        assertThat(employeeFavoriteService.addAssistancePlanFavorite(1L, 5L)).isEqualTo(EmployeeFavoriteResult.Success)
        assertThat(employee.assistancePlanFavorites.map { it.id }).containsExactly(5L)
        assertThat(employeeFavoriteService.deleteAssistancePlanFavorite(1L, 5L)).isEqualTo(EmployeeFavoriteResult.Success)
        assertThat(employee.assistancePlanFavorites).isEmpty()
    }

    @Test
    fun deleteClientFavoritesByClientId_removesTheClientFromEveryEmployee() {
        // Given
        val withFavorite = Employee(id = 1L, clientFavorites = mutableSetOf(Client(id = 9L), Client(id = 4L)))
        val otherFavorite = Employee(id = 2L, clientFavorites = mutableSetOf(Client(id = 4L)))
        val withoutFavorite = Employee(id = 3L)
        whenever(employeeService.getAllEntities()).thenReturn(listOf(withFavorite, otherFavorite, withoutFavorite))

        // When
        val removed = employeeFavoriteService.deleteClientFavoritesByClientId(9L)

        // Then
        assertThat(removed).isEqualTo(1)
        assertThat(withFavorite.clientFavorites.map { it.id }).containsExactly(4L)
        verify(employeeService).saveEntity(withFavorite)
        verify(employeeService, never()).saveEntity(otherFavorite)
        verify(employeeService, never()).saveEntity(withoutFavorite)
    }

    @Test
    fun deleteAssistancePlanFavoritesByClientId_removesOnlyPlansOfThatClient() {
        // Given
        val client = Client(id = 9L)
        val employee = Employee(
            id = 1L,
            assistancePlanFavorites = mutableSetOf(
                AssistancePlan(id = 1L, client = client),
                AssistancePlan(id = 2L, client = client),
                AssistancePlan(id = 3L, client = Client(id = 10L))
            )
        )
        whenever(employeeService.getAllEntities()).thenReturn(listOf(employee))

        // When
        val removed = employeeFavoriteService.deleteAssistancePlanFavoritesByClientId(9L)

        // Then
        assertThat(removed).isEqualTo(2)
        assertThat(employee.assistancePlanFavorites.map { it.id }).containsExactly(3L)
        verify(employeeService).saveEntity(employee)
    }
}
