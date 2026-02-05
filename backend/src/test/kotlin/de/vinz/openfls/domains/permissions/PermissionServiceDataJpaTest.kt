package de.vinz.openfls.domains.permissions

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(PermissionService::class, TestBeans::class)
class PermissionServiceDataJpaTest {

    @Autowired
    lateinit var permissionService: PermissionService

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Test
    fun savePermission_dto_persistsEntity() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val dto = PermissionDto().apply {
            employeeId = employee.id!!
            institutionId = institution.id!!
            readEntries = true
        }

        // When
        val result = permissionService.savePermission(dto)

        // Then
        val saved = permissionRepository.findByIds(employee.id!!, institution.id!!)
        assertThat(saved).isNotNull
        assertThat(result.readEntries).isTrue()
    }

    @Test
    fun savePermission_entityMissingIds_throwsException() {
        // Given
        val permission = Permission(id = EmployeeInstitutionRightsKey(employeeId = null, institutionId = null))

        // When / Then
        assertThatThrownBy { permissionService.savePermission(permission) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun savePermission_missingEmployee_throwsException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val permission = Permission(id = EmployeeInstitutionRightsKey(employeeId = 9999, institutionId = institution.id))

        // When / Then
        assertThatThrownBy { permissionService.savePermission(permission) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun savePermissionByInstitution_missingEmployeeId_throwsException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val permission = Permission(id = EmployeeInstitutionRightsKey(employeeId = null, institutionId = institution.id))

        // When / Then
        assertThatThrownBy { permissionService.savePermissionByInstitution(permission, institution) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun savePermissionByInstitution_valid_persistsEntity() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val permission = Permission(id = EmployeeInstitutionRightsKey(employeeId = employee.id, institutionId = institution.id))

        // When
        val result = permissionService.savePermissionByInstitution(permission, institution)

        // Then
        val saved = permissionRepository.findByIds(employee.id!!, institution.id!!)
        assertThat(saved).isNotNull
        assertThat(result.id.employeeId).isEqualTo(employee.id)
    }
}
