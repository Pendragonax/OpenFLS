package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.employees.dto.EmployeeDeleteResult
import de.vinz.openfls.domains.employees.dto.EmployeeDetailResponse
import de.vinz.openfls.domains.services.service.ServiceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EmployeeDeletionServiceTest {

    private val employeeService: EmployeeService = mock()
    private val serviceService: ServiceService = mock()
    private val employeeDeletionService = EmployeeDeletionService(employeeService, serviceService)

    @Test
    fun delete_unknownEmployee_returnsNotFound() {
        // Given
        whenever(employeeService.getEmployeeDetailById(5L, true)).thenReturn(null)

        // When
        val result = employeeDeletionService.delete(5L)

        // Then
        assertThat(result).isEqualTo(EmployeeDeleteResult.NotFound)
        verify(employeeService, never()).deleteById(5L)
    }

    @Test
    fun delete_employeeWithServices_returnsHasServicesAndKeepsTheEmployee() {
        // Given
        whenever(employeeService.getEmployeeDetailById(5L, true)).thenReturn(EmployeeDetailResponse(id = 5L))
        whenever(serviceService.countServicesByEmployeeId(5L)).thenReturn(3L)

        // When
        val result = employeeDeletionService.delete(5L)

        // Then
        assertThat(result).isEqualTo(EmployeeDeleteResult.HasServices)
        verify(employeeService, never()).deleteById(5L)
    }

    @Test
    fun delete_employeeWithoutServices_deletesAndReturnsTheDeletedEmployee() {
        // Given
        val employee = EmployeeDetailResponse(id = 5L, firstName = "Max")
        whenever(employeeService.getEmployeeDetailById(5L, true)).thenReturn(employee)
        whenever(serviceService.countServicesByEmployeeId(5L)).thenReturn(0L)

        // When
        val result = employeeDeletionService.delete(5L)

        // Then
        assertThat(result).isEqualTo(EmployeeDeleteResult.Success(employee))
        verify(employeeService).deleteById(5L)
    }
}
