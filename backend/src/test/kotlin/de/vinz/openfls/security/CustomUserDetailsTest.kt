package de.vinz.openfls.security

import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomUserDetailsTest {

    @Test
    fun isEnabled_inactiveEmployee_returnsFalse() {
        // Given
        val employee = Employee(inactive = true)
        val access = EmployeeAccess(username = "inactive", password = "secret", role = 3, employee = employee)

        // When
        val result = CustomUserDetails(access)

        // Then
        assertThat(result.isEnabled()).isFalse()
    }

    @Test
    fun isEnabled_activeEmployee_returnsTrue() {
        // Given
        val employee = Employee(inactive = false)
        val access = EmployeeAccess(username = "active", password = "secret", role = 3, employee = employee)

        // When
        val result = CustomUserDetails(access)

        // Then
        assertThat(result.isEnabled()).isTrue()
    }
}
