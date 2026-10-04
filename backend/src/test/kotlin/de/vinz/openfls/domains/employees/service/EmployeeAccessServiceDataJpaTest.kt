package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(EmployeeAccessService::class)
class EmployeeAccessServiceDataJpaTest {

    @Autowired
    lateinit var employeeAccessService: EmployeeAccessService

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun hasAnyAccess_withoutAccesses_returnsFalse() {
        assertThat(employeeAccessService.existsAnyAccess()).isFalse()
    }

    @Test
    fun hasAnyAccess_withAccess_returnsTrue() {
        // Given
        employeeWithAccess("maxuser", role = 3)

        // When / Then
        assertThat(employeeAccessService.existsAnyAccess()).isTrue()
    }

    @Test
    fun getEntityByUsername_knownUsername_returnsAccess() {
        // Given
        val employee = employeeWithAccess("maxuser", role = 3)

        // When
        val result = employeeAccessService.getEntityByUsername("maxuser")

        // Then
        assertThat(result?.id).isEqualTo(employee.id)
        assertThat(employeeAccessService.getEntityByUsername("unknown")).isNull()
    }

    @Test
    fun existsByUsername_reflectsStoredUsernames() {
        // Given
        employeeWithAccess("maxuser", role = 3)

        // When / Then
        assertThat(employeeAccessService.existsByUsername("maxuser")).isTrue()
        assertThat(employeeAccessService.existsByUsername("other")).isFalse()
    }

    @Test
    fun changePassword_knownEmployee_replacesThePassword() {
        // Given
        val employee = employeeWithAccess("maxuser", role = 3)

        // When
        val changed = employeeAccessService.changePassword(employee.id!!, "encoded-new")

        // Then
        entityManager.clear()
        assertThat(changed).isTrue()
        assertThat(employeeAccessService.getPasswordHashById(employee.id!!)).isEqualTo("encoded-new")
    }

    @Test
    fun changePassword_unknownEmployee_returnsFalse() {
        assertThat(employeeAccessService.changePassword(9999, "encoded-new")).isFalse()
        assertThat(employeeAccessService.getPasswordHashById(9999)).isNull()
    }

    @Test
    fun isAdminById_onlyTrueForTheAdminRole() {
        // Given
        val admin = employeeWithAccess("adminuser", role = 1)
        val user = employeeWithAccess("normaluser", role = 3)

        // When / Then
        assertThat(employeeAccessService.isAdminById(admin.id!!)).isTrue()
        assertThat(employeeAccessService.isAdminById(user.id!!)).isFalse()
        assertThat(employeeAccessService.isAdminById(9999)).isFalse()
    }

    private fun employeeWithAccess(username: String, role: Int): Employee {
        val employee = Employee(firstname = "Max", lastname = "Mustermann")
        employee.access = EmployeeAccess(username = username, password = "encoded-old", role = role, employee = employee)
        return employeeRepository.save(employee)
    }
}
