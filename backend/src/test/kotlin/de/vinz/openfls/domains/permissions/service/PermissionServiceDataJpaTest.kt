package de.vinz.openfls.domains.permissions.service

import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.permissions.entity.PermissionKey
import de.vinz.openfls.domains.employees.service.EmployeeAccessService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.permissions.dto.PermissionRequest
import de.vinz.openfls.domains.permissions.entity.Permission
import de.vinz.openfls.domains.permissions.repository.PermissionRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(PermissionService::class, EmployeeAccessService::class, TestBeans::class)
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
    fun savePermissionEntity_validPermission_persistsEntity() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val permission = Permission(
            id = PermissionKey(employeeId = employee.id, institutionId = institution.id), employee = employee, institution = institution,
            readEntries = true
        )

        // When
        val result = permissionService.savePermissionEntity(permission)

        // Then
        assertThat(result.readEntries).isTrue()
        val saved = permissionRepository.findByEmployeeId(employee.id!!).toList()
        assertThat(saved).hasSize(1)
    }

    @Test
    fun savePermissionEntity_missingIds_throwsException() {
        // Given
        val permission = Permission(id = PermissionKey(employeeId = null, institutionId = null))

        // When / Then
        assertThatThrownBy { permissionService.savePermissionEntity(permission) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun savePermissionEntity_withoutEmployee_throwsException() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val permission = Permission(
            id = PermissionKey(employeeId = 9999, institutionId = institution.id),
            institution = institution
        )

        // When / Then
        assertThatThrownBy { permissionService.savePermissionEntity(permission) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun savePermissionEntity_withoutInstitution_throwsException() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val permission = Permission(
            id = PermissionKey(employeeId = employee.id, institutionId = 9999),
            employee = employee
        )

        // When / Then
        assertThatThrownBy { permissionService.savePermissionEntity(permission) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun getAllEntitiesByEmployeeId_returnsPersistedPermissions() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        permissionService.savePermissionEntity(
            Permission(id = PermissionKey(employeeId = employee.id, institutionId = institution.id), employee = employee, institution = institution)
        )

        // When
        val result = permissionService.getAllEntitiesByEmployeeId(employee.id!!)

        // Then
        assertThat(result).hasSize(1)
    }

    @Test
    fun institutionIdFilters_returnOnlyMatchingPermissions() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val readInstitution = institutionRepository.save(Institution(name = "Read", email = "a@b.c", phonenumber = "1"))
        val writeInstitution = institutionRepository.save(Institution(name = "Write", email = "a@b.c", phonenumber = "2"))
        val leadInstitution = institutionRepository.save(Institution(name = "Lead", email = "a@b.c", phonenumber = "3"))
        val affiliatedInstitution = institutionRepository.save(Institution(name = "Affiliated", email = "a@b.c", phonenumber = "4"))
        permissionService.savePermissionEntity(
            Permission(id = PermissionKey(employeeId = employee.id, institutionId = readInstitution.id), employee = employee, institution = readInstitution, readEntries = true)
        )
        permissionService.savePermissionEntity(
            Permission(id = PermissionKey(employeeId = employee.id, institutionId = writeInstitution.id), employee = employee, institution = writeInstitution, writeEntries = true)
        )
        permissionService.savePermissionEntity(
            Permission(id = PermissionKey(employeeId = employee.id, institutionId = leadInstitution.id), employee = employee, institution = leadInstitution, changeInstitution = true)
        )
        permissionService.savePermissionEntity(
            Permission(id = PermissionKey(employeeId = employee.id, institutionId = affiliatedInstitution.id), employee = employee, institution = affiliatedInstitution, affiliated = true)
        )

        // When / Then
        assertThat(permissionService.getReadableInstitutionIdsByEmployee(employee.id!!)).containsExactly(readInstitution.id)
        assertThat(permissionService.getWritableInstitutionIdsByEmployee(employee.id!!)).containsExactly(writeInstitution.id)
        assertThat(permissionService.getLeadingInstitutionIdsByEmployee(employee.id!!)).containsExactly(leadInstitution.id)
        assertThat(permissionService.getAffiliatedInstitutionIdsByEmployee(employee.id!!)).containsExactly(affiliatedInstitution.id)
    }

    @Test
    fun convertToPermissions_mapsRequestsToEntitiesForEmployee() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val requests = listOf(
            PermissionRequest(employeeId = employee.id!!, institutionId = institution.id!!, readEntries = true)
        )

        // When
        val result = permissionService.convertToPermissions(requests, employee)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.first().employee).isEqualTo(employee)
        assertThat(result.first().institution).isEqualTo(institution)
        assertThat(result.first().readEntries).isTrue()
    }
}
