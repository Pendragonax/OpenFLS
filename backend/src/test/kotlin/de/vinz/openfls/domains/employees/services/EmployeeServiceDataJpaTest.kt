package de.vinz.openfls.domains.employees.services

import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.UnprofessionalRepository
import de.vinz.openfls.domains.employees.dtos.EmployeeAccessDto
import de.vinz.openfls.domains.employees.dtos.EmployeeDto
import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.employees.entities.UnprofessionalKey
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionDto
import de.vinz.openfls.domains.permissions.PermissionRepository
import de.vinz.openfls.domains.sponsors.Sponsor
import de.vinz.openfls.domains.sponsors.SponsorRepository
import de.vinz.openfls.testsupport.TestBeans
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
@Import(
    EmployeeService::class,
    UnprofessionalService::class,
    de.vinz.openfls.domains.permissions.PermissionService::class,
    TestBeans::class
)
class EmployeeServiceDataJpaTest(@Autowired private val unprofessionalRepository: UnprofessionalRepository) {

    @Autowired
    lateinit var employeeService: EmployeeService

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var employeeAccessRepository: EmployeeAccessRepository

    @Autowired
    lateinit var permissionRepository: PermissionRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun create_dto_persistsEmployeeAndAccess() {
        // Given
        val dto = EmployeeDto().apply {
            firstName = "Max"
            lastName = "Mustermann"
            email = "m@m.de"
            access = EmployeeAccessDto().apply {
                username = "maxuser"
                role = 2
            }
        }

        // When
        val result = employeeService.create(dto)

        // Then
        val savedEmployee = employeeRepository.findById(result.id)
        val savedAccess = employeeAccessRepository.findById(result.id)
        assertThat(savedEmployee).isPresent
        assertThat(savedAccess).isPresent
        assertThat(savedAccess.get().username).isEqualTo("maxuser")
    }

    @Test
    fun create_entityWithoutAccess_throwsException() {
        // Given
        val employee = Employee(firstname = "Max", lastname = "Mustermann")

        // When / Then
        assertThatThrownBy { employeeService.create(employee) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun create_entityWithEmptyPassword_throwsException() {
        // Given
        val employee = Employee(firstname = "Max", lastname = "Mustermann").apply {
            access = EmployeeAccess(username = "maxuser", password = "", role = 2, employee = this)
        }

        // When / Then
        assertThatThrownBy { employeeService.create(employee) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun create_entityWithDuplicateUsername_throwsException() {
        // Given
        val existingDto = EmployeeDto().apply {
            firstName = "Max"
            lastName = "One"
            access = EmployeeAccessDto().apply {
                username = "dupuser"
                role = 2
            }
        }
        employeeService.create(existingDto)
        val employee = Employee(firstname = "Max", lastname = "Two").apply {
            access = EmployeeAccess(username = "dupuser", password = "secret", role = 2, employee = this)
        }

        // When / Then
        assertThatThrownBy { employeeService.create(employee) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_existingEmployee_updatesFields() {
        // Given
        whenever(accessService.isAdmin()).thenReturn(false)
        val existing = employeeRepository.save(Employee(firstname = "Old", lastname = "Name", email = "old@x.de"))
        val dto = EmployeeDto().apply {
            firstName = "New"
            lastName = "Name"
            email = "new@x.de"
            phonenumber = "123"
        }

        // When
        val result = employeeService.update(existing.id!!, dto)

        // Then
        val saved = employeeRepository.findById(existing.id!!)
        assertThat(saved).isPresent
        assertThat(saved.get().firstname).isEqualTo("New")
        assertThat(result.email).isEqualTo("new@x.de")
    }

    @Test
    fun update_existingEmployeeAsAdmin_updatesFieldsAndPermissions() {
        // Given
        whenever(accessService.isAdmin()).thenReturn(true)
        val existingSponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val existingInstitution = institutionRepository.save(Institution(name = "Inst"))
        var existing = Employee(firstname = "Old", lastname = "Name",
            email = "old@x.de");
        val existingAccess = EmployeeAccess(username = "olduser", password = "secret", role = 1, employee = existing)
        existing.access = existingAccess
        existing = employeeRepository.save(existing)

        val permission = Permission().apply {
            id.employeeId = existing.id
            id.institutionId = existingInstitution.id
            employee = existing
            institution = existingInstitution
            readEntries = false
            changeInstitution = true
        }
        permissionRepository.save(permission)

        val unprofessional = Unprofessional().apply {
            id = UnprofessionalKey(
                employeeId = existing.id,
                sponsorId = existingSponsor.id
            )
            employee = existing
            sponsor = existingSponsor
        }
        unprofessionalRepository.save(unprofessional)

        val dto = EmployeeDto().apply {
            firstName = "New"
            lastName = "Name"
            email = "new@x.de"
            phonenumber = "123"
            access = EmployeeAccessDto().apply {
                id = existing.id!!
                username = "updatedUsername"
                role = 2
                password = "updatedPassword"
            }
            permissions = listOf(
                PermissionDto().apply {
                    employeeId = existing.id!!
                    institutionId = existingInstitution.id!!
                    readEntries = true
                    changeInstitution = false
                },
            )
            unprofessionals = listOf(
                UnprofessionalDto().apply {
                    employeeId = existing.id!!
                    sponsorId = existingSponsor.id
                    end = LocalDate.now()
                }
            )
        }

        // When
        employeeService.update(existing.id!!, dto)

        // Then
        val saved = employeeRepository.findById(existing.id!!)
        assertThat(saved).isPresent
        assertThat(saved.get().firstname).isEqualTo("New")
        assertThat(saved.get().email).isEqualTo("new@x.de")
        assertThat(saved.get().access!!.username).isEqualTo(existingAccess.username)
        assertThat(saved.get().access!!.password).isEqualTo(existingAccess.password)
        assertThat(saved.get().access!!.role).isEqualTo(existingAccess.role)
        assertThat(saved.get().permissions).isNotNull().hasSize(1)
        val savedPermission = saved.get().permissions!!.first { it.id.institutionId == existingInstitution.id }
        assertThat(savedPermission.readEntries).isTrue
        assertThat(savedPermission.changeInstitution).isFalse

        assertThat(saved.get().unprofessionals).isNotEmpty()
        val savedUnprofessional = saved.get().unprofessionals!!.first { it.id?.sponsorId == existingSponsor.id }
        assertThat(savedUnprofessional.id?.employeeId).isEqualTo(existing.id)
        assertThat(savedUnprofessional.id?.sponsorId).isEqualTo(existingSponsor.id)
        assertThat(savedUnprofessional.end).isNotNull()
    }

    @Test
    fun update_missingEmployee_throwsException() {
        // Given
        whenever(accessService.isAdmin()).thenReturn(false)
        val dto = EmployeeDto().apply {
            firstName = "New"
            lastName = "Name"
        }

        // When / Then
        assertThatThrownBy { employeeService.update(9999, dto) }
            .isInstanceOf(EntityNotFoundException::class.java)
    }
}
