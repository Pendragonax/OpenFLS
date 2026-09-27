package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import de.vinz.openfls.domains.institutions.dtos.InstitutionCreateRequest
import de.vinz.openfls.domains.institutions.dtos.InstitutionPermissionRequest
import de.vinz.openfls.domains.institutions.dtos.InstitutionUpdateRequest
import de.vinz.openfls.domains.institutions.dtos.InstitutionUpdateResult
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionRepository
import de.vinz.openfls.domains.permissions.PermissionService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(InstitutionService::class, PermissionService::class, TestBeans::class)
class InstitutionServiceDataJpaTest {

    @Autowired
    lateinit var institutionService: InstitutionService

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun create_validRequest_persistsInstitutionAndPermissions() {
        // Given
        val employee1 = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val employee2 = employeeRepository.save(Employee(firstname = "Max", lastname = "Two"))
        val request = InstitutionCreateRequest(
            name = "Inst",
            email = "a@b.c",
            phonenumber = "123",
            permissions = listOf(
                InstitutionPermissionRequest(employeeId = employee1.id!!, readEntries = true),
                InstitutionPermissionRequest(employeeId = employee2.id!!, writeEntries = true)
            )
        )

        // When
        val result = institutionService.create(request)

        // Then
        entityManager.flush()
        entityManager.clear()
        val saved = institutionRepository.findAll().toList()
        assertThat(saved).hasSize(1)
        val permissions = permissionRepository.findByInstitutionId(saved.first().id!!).toList()
        assertThat(permissions).hasSize(2)
        assertThat(permissions.first { it.id.employeeId == employee1.id }.readEntries).isTrue
        assertThat(permissions.first { it.id.employeeId == employee2.id }.writeEntries).isTrue
        assertThat(result.permissions.map { it.employeeId }).containsExactlyInAnyOrder(employee1.id, employee2.id)
        assertThat(result.permissions).allMatch { it.institutionId == result.id }
    }

    @Test
    fun create_unknownEmployee_throwsException() {
        // Given
        val request = InstitutionCreateRequest(
            name = "Inst",
            permissions = listOf(InstitutionPermissionRequest(employeeId = 9999, readEntries = true))
        )

        // When / Then
        assertThatThrownBy { institutionService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_missingInstitution_returnsNotFound() {
        // Given
        val request = InstitutionUpdateRequest(id = 9999, name = "Missing")

        // When
        val result = institutionService.update(request)

        // Then
        assertThat(result).isEqualTo(InstitutionUpdateResult.NotFound)
    }

    @Test
    fun update_existingInstitution_updatesFieldsAndSynchronizesPermissions() {
        // Given
        val kept = employeeRepository.save(Employee(firstname = "Max", lastname = "Kept"))
        val removed = employeeRepository.save(Employee(firstname = "Max", lastname = "Removed"))
        val added = employeeRepository.save(Employee(firstname = "Max", lastname = "Added"))
        val institution = institutionRepository.save(Institution(name = "Old", email = "o@o.de", phonenumber = "1"))
        savePermission(kept, institution, read = true)
        savePermission(removed, institution, write = true)
        entityManager.flush()
        entityManager.clear()

        val request = InstitutionUpdateRequest(
            id = institution.id!!,
            name = "New",
            email = "n@n.de",
            phonenumber = "2",
            permissions = listOf(
                InstitutionPermissionRequest(employeeId = kept.id!!, readEntries = false, changeInstitution = true),
                InstitutionPermissionRequest(employeeId = added.id!!, affiliated = true)
            )
        )

        // When
        val result = institutionService.update(request) as InstitutionUpdateResult.Success

        // Then
        entityManager.flush()
        entityManager.clear()
        val saved = institutionRepository.findById(institution.id!!).get()
        assertThat(saved.name).isEqualTo("New")
        assertThat(saved.phonenumber).isEqualTo("2")
        val permissions = permissionRepository.findByInstitutionId(institution.id!!).toList()
        assertThat(permissions.map { it.id.employeeId }).containsExactlyInAnyOrder(kept.id, added.id)
        assertThat(permissions.first { it.id.employeeId == kept.id }.readEntries).isFalse
        assertThat(permissions.first { it.id.employeeId == kept.id }.changeInstitution).isTrue
        assertThat(permissions.first { it.id.employeeId == added.id }.affiliated).isTrue
        assertThat(result.response.permissions.map { it.employeeId }).containsExactlyInAnyOrder(kept.id, added.id)
    }

    @Test
    fun getAll_returnsFlatInstitutionsSortedByName() {
        // Given
        institutionRepository.save(Institution(name = "Beta"))
        institutionRepository.save(Institution(name = "Alpha"))

        // When
        val result = institutionService.getAll()

        // Then
        assertThat(result.map { it.name }).containsExactly("Alpha", "Beta")
    }

    @Test
    fun getAllWithPermissions_includesPermissions() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val institution = institutionRepository.save(Institution(name = "Inst"))
        savePermission(employee, institution, read = true)
        entityManager.flush()
        entityManager.clear()

        // When
        val result = institutionService.getAllWithPermissions()

        // Then
        assertThat(result.single().permissions.single().readEntries).isTrue
    }

    @Test
    fun getWithPermissionsById_missingInstitution_returnsNull() {
        assertThat(institutionService.getWithPermissionsById(9999)).isNull()
    }

    @Test
    fun getEntityById_existingInstitution_returnsEntity() {
        // Given
        val institution = institutionRepository.save(Institution(name = "Inst"))

        // When / Then
        assertThat(institutionService.getEntityById(institution.id!!)).isEqualTo(institution)
    }

    private fun savePermission(
        employee: Employee,
        institution: Institution,
        read: Boolean = false,
        write: Boolean = false
    ) {
        permissionRepository.save(
            Permission(
                id = EmployeeInstitutionRightsKey(employeeId = employee.id, institutionId = institution.id),
                employee = employee,
                institution = institution,
                readEntries = read,
                writeEntries = write
            )
        )
    }
}
