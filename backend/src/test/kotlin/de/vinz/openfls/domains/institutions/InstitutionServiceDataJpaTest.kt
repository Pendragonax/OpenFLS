package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import de.vinz.openfls.domains.institutions.dtos.CreateInstitutionDTO
import de.vinz.openfls.domains.institutions.dtos.UpdateInstitutionDTO
import de.vinz.openfls.domains.permissions.PermissionDto
import de.vinz.openfls.domains.permissions.PermissionRepository
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(InstitutionService::class, TestBeans::class)
class InstitutionServiceDataJpaTest {

    @Autowired
    lateinit var institutionService: InstitutionService

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Test
    fun create_validDto_persistsInstitutionAndPermissions() {
        // Given
        val employee1 = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val employee2 = employeeRepository.save(Employee(firstname = "Max", lastname = "Two"))
        val dto = CreateInstitutionDTO(
            name = "Inst",
            email = "a@b.c",
            phonenumber = "123",
            permissions = listOf(
                PermissionDto().apply {
                    employeeId = employee1.id!!
                    institutionId = 0
                    readEntries = true
                },
                PermissionDto().apply {
                    employeeId = employee2.id!!
                    institutionId = 0
                    writeEntries = true
                }
            )
        )

        // When
        institutionService.create(dto)

        // Then
        val saved = institutionRepository.findAll().toList()
        assertThat(saved).hasSize(1)
        val permissions = permissionRepository.findByInstitutionId(saved.first().id!!)
        assertThat(permissions).hasSize(2)

        for (permission in permissions) {
            when (permission.id.employeeId) {
                employee1.id -> assertThat(permission.readEntries).isTrue
                employee2.id -> assertThat(permission.writeEntries).isTrue
                else -> throw IllegalStateException("Unexpected employee ID")
            }
        }
    }

    @Test
    fun update_missingInstitution_throwsException() {
        // Given
        val dto = UpdateInstitutionDTO(
            id = 9999,
            name = "Missing",
            email = "x@y.z",
            phonenumber = "000",
            permissions = emptyList()
        )

        // When / Then
        assertThatThrownBy { institutionService.update(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_existingInstitution_updatesPermissionsAndFields() {
        // Given
        val employee1 = employeeRepository.save(Employee(firstname = "Max", lastname = "One"))
        val employee2 = employeeRepository.save(Employee(firstname = "Max", lastname = "Two"))
        val institution = institutionRepository.save(Institution(name = "Old", email = "o@o.de", phonenumber = "1"))
        permissionRepository.save(
            de.vinz.openfls.domains.permissions.Permission(
                id = EmployeeInstitutionRightsKey(employeeId = employee1.id, institutionId = institution.id),
                employee = employee1,
                institution = institution,
                readEntries = true
            )
        )
        permissionRepository.save(
            de.vinz.openfls.domains.permissions.Permission(
                id = EmployeeInstitutionRightsKey(employeeId = employee2.id, institutionId = institution.id),
                employee = employee2,
                institution = institution,
                writeEntries = true
            )
        )

        val updatedDto = UpdateInstitutionDTO(
            id = institution.id!!,
            name = "New",
            email = "n@n.de",
            phonenumber = "2",
            permissions = listOf(
                PermissionDto().apply {
                    employeeId = employee1.id!!
                    institutionId = institution.id!!
                    readEntries = false
                    changeInstitution = true
                },
                PermissionDto().apply {
                    employeeId = employee2.id!!
                    institutionId = institution.id!!
                    readEntries = true
                    changeInstitution = false
                }
            )
        )

        // When
        institutionService.update(updatedDto)

        // Then
        val saved = institutionRepository.findById(institution.id!!)
        assertThat(saved).isPresent
        assertThat(saved.get().name).isEqualTo("New")
        val permissions = permissionRepository.findByInstitutionId(institution.id!!)
        assertThat(permissions.map { it.id.employeeId }).containsExactlyInAnyOrder(employee1.id, employee2.id)

        for (permission in permissions) {
            when (permission.id.employeeId) {
                employee1.id -> {
                    assertThat(permission.readEntries).isFalse
                    assertThat(permission.changeInstitution).isTrue
                }
                employee2.id -> {
                    assertThat(permission.readEntries).isTrue
                    assertThat(permission.changeInstitution).isFalse
                }
                else -> throw IllegalStateException("Unexpected employee ID")
            }
        }
    }
}
