package de.vinz.openfls.domains.employees.service

import jakarta.persistence.EntityManagerFactory
import de.vinz.openfls.testsupport.QueryCounter
import de.vinz.openfls.domains.institutions.service.InstitutionLookupService
import de.vinz.openfls.domains.employees.dto.EmployeeCreateAccessRequest
import de.vinz.openfls.domains.employees.dto.EmployeeCreateRequest
import de.vinz.openfls.domains.employees.dto.EmployeeCreateResult
import de.vinz.openfls.domains.employees.dto.EmployeePasswordResetResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateRoleResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateRequest
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateResult
import de.vinz.openfls.domains.employees.dto.UnprofessionalRequest
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import de.vinz.openfls.domains.employees.entity.Unprofessional
import de.vinz.openfls.domains.employees.entity.UnprofessionalKey
import de.vinz.openfls.domains.employees.repository.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.employees.repository.UnprofessionalRepository
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.permissions.dto.PermissionRequest
import de.vinz.openfls.domains.permissions.entity.Permission
import de.vinz.openfls.domains.permissions.entity.PermissionKey
import de.vinz.openfls.domains.permissions.repository.PermissionRepository
import de.vinz.openfls.domains.permissions.service.PermissionService
import de.vinz.openfls.domains.sponsors.entity.Sponsor
import de.vinz.openfls.domains.sponsors.repository.SponsorRepository
import de.vinz.openfls.domains.sponsors.service.SponsorService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

@DataJpaTest
@Import(
    EmployeeService::class,
    EmployeeAccessService::class,
    UnprofessionalService::class,
    SponsorService::class,
    PermissionService::class,
    InstitutionLookupService::class,
    TestBeans::class
)
class EmployeeServiceDataJpaTest(@param:Autowired private val unprofessionalRepository: UnprofessionalRepository) {

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

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @Test
    fun create_validRequest_persistsEmployeeAndAccess() {
        // Given
        val request = createRequest(username = "maxuser", role = 2)

        // When
        val result = employeeService.create(request)

        // Then
        val response = (result as EmployeeCreateResult.Success).response
        assertThat(employeeRepository.findById(response.id)).isPresent
        val savedAccess = employeeAccessRepository.findById(response.id)
        assertThat(savedAccess).isPresent
        assertThat(savedAccess.get().username).isEqualTo("maxuser")
        assertThat(savedAccess.get().role).isEqualTo(2)
        assertThat(response.access?.username).isEqualTo("maxuser")
    }

    @Test
    fun create_validRequest_startsWithTheUsernameAsEncodedPassword() {
        // When
        val result = employeeService.create(createRequest(username = "maxuser"))

        // Then
        val id = (result as EmployeeCreateResult.Success).response.id
        val password = employeeAccessRepository.findById(id).get().password
        assertThat(password).isNotEqualTo("maxuser")
        assertThat(passwordEncoder.matches("maxuser", password)).isTrue()
    }

    @Test
    fun create_usernameWithUmlautsAndDigits_persistsEmployeeAndAccess() {
        // When
        val result = employeeService.create(createRequest(username = "Mäx123"))

        // Then
        val id = (result as EmployeeCreateResult.Success).response.id
        assertThat(employeeAccessRepository.findById(id).get().username).isEqualTo("Mäx123")
    }

    @Test
    fun create_usernameWithWhitespace_returnsInvalidUsername() {
        // When
        val result = employeeService.create(createRequest(username = "max user"))

        // Then
        assertThat(result).isEqualTo(EmployeeCreateResult.InvalidUsername("username contains invalid characters"))
        assertThat(employeeRepository.count()).isZero()
    }

    @Test
    fun create_usernameWithSpecialCharacter_returnsInvalidUsername() {
        // When
        val result = employeeService.create(createRequest(username = "max\$user"))

        // Then
        assertThat(result).isEqualTo(EmployeeCreateResult.InvalidUsername("username contains invalid characters"))
    }

    @Test
    fun create_tooShortUsername_returnsInvalidUsername() {
        // When
        val result = employeeService.create(createRequest(username = "max"))

        // Then
        assertThat(result).isEqualTo(EmployeeCreateResult.InvalidUsername("username is too short"))
    }

    @Test
    fun create_duplicateUsername_returnsUsernameTaken() {
        // Given
        employeeService.create(createRequest(username = "dupuser", lastName = "One"))

        // When
        val result = employeeService.create(createRequest(username = "dupuser", lastName = "Two"))

        // Then
        assertThat(result).isEqualTo(EmployeeCreateResult.UsernameTaken)
        assertThat(employeeRepository.count()).isEqualTo(1)
    }

    @Test
    fun create_unknownRole_returnsInvalidRole() {
        // When
        val result = employeeService.create(createRequest(username = "maxuser", role = 7))

        // Then
        assertThat(result).isEqualTo(EmployeeCreateResult.InvalidRole)
        assertThat(employeeRepository.count()).isZero()
    }

    @Test
    fun create_unknownSponsor_returnsSponsorNotFoundAndPersistsNothing() {
        // Given
        val request = createRequest(username = "maxuser").apply {
            unprofessionals = listOf(UnprofessionalRequest(sponsorId = 9999, end = LocalDate.now()))
        }

        // When
        val result = employeeService.create(request)

        // Then
        assertThat(result).isEqualTo(EmployeeCreateResult.SponsorNotFound)
        assertThat(employeeRepository.count()).isZero()
    }

    @Test
    fun create_withUnprofessional_persistsUnprofessionalForTheNewEmployee() {
        // Given
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val request = createRequest(username = "maxuser").apply {
            unprofessionals = listOf(UnprofessionalRequest(sponsorId = sponsor.id, end = LocalDate.of(2026, 5, 1)))
        }

        // When
        val result = employeeService.create(request)

        // Then
        val response = (result as EmployeeCreateResult.Success).response
        val saved = unprofessionalRepository.findByEmployeeId(response.id)
        assertThat(saved).hasSize(1)
        assertThat(saved.single().id?.sponsorId).isEqualTo(sponsor.id)
        assertThat(saved.single().end).isEqualTo(LocalDate.of(2026, 5, 1))
        assertThat(response.unprofessionals.single().sponsorId).isEqualTo(sponsor.id)
    }

    @Test
    fun update_existingEmployee_updatesFields() {
        // Given
        val existing = employeeRepository.save(Employee(firstname = "Old", lastname = "Name", email = "old@x.de"))
        val request = EmployeeUpdateRequest().apply {
            id = existing.id!!
            firstName = "New"
            lastName = "Name"
            email = "new@x.de"
            phonenumber = "123"
            description = "Neue Beschreibung"
        }

        // When
        val result = employeeService.update(existing.id!!, request)

        // Then
        val response = (result as EmployeeUpdateResult.Success).response
        val saved = employeeRepository.findById(existing.id!!).get()
        assertThat(saved.firstname).isEqualTo("New")
        assertThat(saved.description).isEqualTo("Neue Beschreibung")
        assertThat(response.email).isEqualTo("new@x.de")
    }

    @Test
    fun update_withPermissionsAndUnprofessionals_replacesThemAndKeepsAccess() {
        // Given
        val existingSponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val existingInstitution = institutionRepository.save(Institution(name = "Inst"))
        var existing = Employee(firstname = "Old", lastname = "Name", email = "old@x.de")
        val existingAccess = EmployeeAccess(username = "olduser", password = "secret", role = 1, employee = existing)
        existing.access = existingAccess
        existing = employeeRepository.save(existing)

        permissionRepository.save(Permission().apply {
            id.employeeId = existing.id
            id.institutionId = existingInstitution.id
            employee = existing
            institution = existingInstitution
            readEntries = false
            changeInstitution = true
        })
        unprofessionalRepository.save(Unprofessional().apply {
            id = UnprofessionalKey(employeeId = existing.id, sponsorId = existingSponsor.id)
            employee = existing
            sponsor = existingSponsor
        })

        val request = EmployeeUpdateRequest().apply {
            id = existing.id!!
            firstName = "New"
            lastName = "Name"
            email = "new@x.de"
            phonenumber = "123"
            permissions = listOf(
                PermissionRequest(
                    employeeId = existing.id!!,
                    institutionId = existingInstitution.id!!,
                    readEntries = true,
                    changeInstitution = false
                )
            )
            unprofessionals = listOf(UnprofessionalRequest(sponsorId = existingSponsor.id, end = LocalDate.now()))
        }

        // When
        val result = employeeService.update(existing.id!!, request)

        // Then
        val response = (result as EmployeeUpdateResult.Success).response
        val saved = employeeRepository.findById(existing.id!!).get()
        assertThat(saved.access!!.username).isEqualTo(existingAccess.username)
        assertThat(saved.access!!.password).isEqualTo(existingAccess.password)
        assertThat(saved.access!!.role).isEqualTo(existingAccess.role)
        assertThat(saved.permissions).isNotNull().hasSize(1)
        val savedPermission = saved.permissions!!.first { it.id.institutionId == existingInstitution.id }
        assertThat(savedPermission.readEntries).isTrue
        assertThat(savedPermission.changeInstitution).isFalse

        // the response must reflect the saved values
        val returnedPermission = response.permissions.single { it.institutionId == existingInstitution.id }
        assertThat(returnedPermission.readEntries).isTrue
        assertThat(returnedPermission.changeInstitution).isFalse

        val savedUnprofessional = saved.unprofessionals!!.single { it.id?.sponsorId == existingSponsor.id }
        assertThat(savedUnprofessional.id?.employeeId).isEqualTo(existing.id)
        assertThat(savedUnprofessional.end).isNotNull()
    }

    @Test
    fun update_removedUnprofessional_isDeleted() {
        // Given
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor"))
        val existing = employeeRepository.save(Employee(firstname = "Old", lastname = "Name"))
        unprofessionalRepository.save(Unprofessional(
            id = UnprofessionalKey(employeeId = existing.id, sponsorId = sponsor.id),
            employee = existing,
            sponsor = sponsor
        ))
        val request = EmployeeUpdateRequest().apply {
            id = existing.id!!
            firstName = "Old"
            lastName = "Name"
        }

        // When
        employeeService.update(existing.id!!, request)

        // Then
        assertThat(unprofessionalRepository.findByEmployeeId(existing.id!!)).isEmpty()
    }

    @Test
    fun update_missingEmployee_returnsNotFound() {
        // Given
        val request = EmployeeUpdateRequest().apply {
            firstName = "New"
            lastName = "Name"
        }

        // When / Then
        assertThat(employeeService.update(9999, request)).isEqualTo(EmployeeUpdateResult.NotFound)
    }

    @Test
    fun update_unknownSponsor_returnsSponsorNotFoundAndKeepsFields() {
        // Given
        val existing = employeeRepository.save(Employee(firstname = "Old", lastname = "Name"))
        val request = EmployeeUpdateRequest().apply {
            id = existing.id!!
            firstName = "New"
            lastName = "Name"
            unprofessionals = listOf(UnprofessionalRequest(sponsorId = 9999))
        }

        // When
        val result = employeeService.update(existing.id!!, request)

        // Then
        assertThat(result).isEqualTo(EmployeeUpdateResult.SponsorNotFound)
        assertThat(employeeRepository.findById(existing.id!!).get().firstname).isEqualTo("Old")
    }

    @Test
    fun updateRole_validRole_changesOnlyTheRole() {
        // Given
        val employee = employeeWithAccess(username = "olduser", password = "encoded-secret", role = 3)

        // When
        val result = employeeService.updateRole(employee.id!!, 2)

        // Then
        assertThat(result).isInstanceOf(EmployeeUpdateRoleResult.Success::class.java)
        entityManager.flush()
        entityManager.clear()
        val saved = employeeAccessRepository.findById(employee.id!!).get()
        assertThat(saved.role).isEqualTo(2)
        assertThat(saved.password).isEqualTo("encoded-secret")
        assertThat(saved.username).isEqualTo("olduser")
    }

    @Test
    fun updateRole_unknownRole_returnsInvalidRoleAndKeepsRole() {
        // Given
        val employee = employeeWithAccess(username = "olduser", password = "encoded-secret", role = 3)

        // When / Then
        assertThat(employeeService.updateRole(employee.id!!, 0)).isEqualTo(EmployeeUpdateRoleResult.InvalidRole)
        assertThat(employeeService.updateRole(employee.id!!, 4)).isEqualTo(EmployeeUpdateRoleResult.InvalidRole)
        entityManager.flush()
        entityManager.clear()
        assertThat(employeeAccessRepository.findById(employee.id!!).get().role).isEqualTo(3)
    }

    @Test
    fun updateRole_missingEmployee_returnsNotFound() {
        assertThat(employeeService.updateRole(9999, 2)).isEqualTo(EmployeeUpdateRoleResult.NotFound)
    }

    @Test
    fun resetPassword_existingEmployee_setsUsernameAsEncodedPassword() {
        // Given
        val employee = employeeWithAccess(username = "olduser", password = "encoded-secret", role = 3)

        // When
        val result = employeeService.resetPassword(employee.id!!)

        // Then
        assertThat(result).isInstanceOf(EmployeePasswordResetResult.Success::class.java)
        entityManager.flush()
        entityManager.clear()
        val password = employeeAccessRepository.findById(employee.id!!).get().password
        assertThat(passwordEncoder.matches("olduser", password)).isTrue()
    }

    @Test
    fun resetPassword_missingEmployee_returnsNotFound() {
        assertThat(employeeService.resetPassword(9999)).isEqualTo(EmployeePasswordResetResult.NotFound)
    }

    @Test
    fun deleteById_removesEmployeeAndAccess() {
        // Given
        val employee = employeeWithAccess(username = "olduser", password = "encoded-secret", role = 3)

        // When
        employeeService.deleteById(employee.id!!)
        entityManager.flush()
        entityManager.clear()

        // Then
        assertThat(employeeRepository.findById(employee.id!!)).isEmpty
        assertThat(employeeAccessRepository.findById(employee.id!!)).isEmpty
    }

    @Test
    fun getAllEmployees_withoutArchived_hidesArchivedEmployees() {
        // Given
        employeeRepository.save(Employee(firstname = "Active", lastname = "Alpha"))
        employeeRepository.save(Employee(firstname = "Archived", lastname = "Zulu", archived = true))

        // When
        val result = employeeService.getAllEmployeeDetails(includeArchived = false)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.single().firstName).isEqualTo("Active")
    }

    @Test
    fun getAllEmployees_includeArchived_returnsArchivedEmployeesSortedByLastName() {
        // Given
        employeeRepository.save(Employee(firstname = "Archived", lastname = "Zulu", archived = true))
        employeeRepository.save(Employee(firstname = "Active", lastname = "alpha"))

        // When
        val result = employeeService.getAllEmployeeDetails(includeArchived = true)

        // Then
        assertThat(result.map { it.lastName }).containsExactly("alpha", "Zulu")
        assertThat(result.last().archived).isTrue()
    }

    @Test
    fun getAllEmployees_neverExposesThePassword() {
        // Given
        employeeWithAccess(username = "olduser", password = "encoded-secret", role = 3)

        // When
        val result = employeeService.getAllEmployeeDetails(includeArchived = false)

        // Then
        assertThat(result.single().access?.username).isEqualTo("olduser")
        assertThat(result.single().toString()).doesNotContain("encoded-secret")
    }

    @Test
    fun getEmployeeWithAccessById_archivedEmployeeWithoutArchived_returnsNull() {
        // Given
        val archived = employeeRepository.save(Employee(firstname = "Archived", lastname = "Alpha", archived = true))

        // When / Then
        assertThat(employeeService.getEmployeeDetailById(archived.id!!, includeArchived = false)).isNull()
        assertThat(employeeService.getEmployeeDetailById(archived.id!!, includeArchived = true)).isNotNull
    }

    @Test
    fun getEmployeeById_archivedEmployeeWithoutArchived_returnsNull() {
        // Given
        val archived = employeeRepository.save(Employee(firstname = "Archived", lastname = "Alpha", archived = true))

        // When / Then
        assertThat(employeeService.getEmployeeNameById(archived.id!!, includeArchived = false)).isNull()
        assertThat(employeeService.getEmployeeNameById(archived.id!!, includeArchived = true)?.firstName)
            .isEqualTo("Archived")
    }

    private fun createRequest(username: String, role: Int = 3, lastName: String = "Mustermann") =
        EmployeeCreateRequest().apply {
            firstName = "Max"
            this.lastName = lastName
            email = "m@m.de"
            access = EmployeeCreateAccessRequest().apply {
                this.username = username
                this.role = role
            }
        }

    private fun employeeWithAccess(username: String, password: String, role: Int): Employee {
        val employee = Employee(firstname = "Old", lastname = "Name")
        employee.access = EmployeeAccess(username = username, password = password, role = role, employee = employee)
        return employeeRepository.save(employee)
    }

    @Test
    fun getAllEmployeeDetails_loadsAccessPermissionsAndUnprofessionalsWithAConstantNumberOfQueries() {
        // Given
        val institutionId = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1")).id!!
        val sponsorId = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false)).id
        repeat(3) { createDetailedEmployee(it, institutionId, sponsorId) }
        entityManager.flush()
        entityManager.clear()
        val queryCounter = QueryCounter(entityManagerFactory)
        val queriesForFewEmployees = queryCounter.count { employeeService.getAllEmployeeDetails(includeArchived = true) }

        repeat(17) { createDetailedEmployee(it + 3, institutionId, sponsorId) }
        entityManager.flush()
        entityManager.clear()

        // When
        val queriesForManyEmployees = queryCounter.count { employeeService.getAllEmployeeDetails(includeArchived = true) }

        // Then
        assertThat(queriesForManyEmployees).isEqualTo(queriesForFewEmployees)
    }

    private fun createDetailedEmployee(index: Int, institutionId: Long, sponsorId: Long) {
        val employee = Employee(firstname = "Max", lastname = "Employee $index")
        employee.access = EmployeeAccess(username = "employee$index", password = "secret", role = 3, employee = employee)
        val saved = employeeRepository.save(employee)
        permissionRepository.save(
            Permission(
                id = PermissionKey(employeeId = saved.id, institutionId = institutionId),
                employee = saved,
                institution = institutionRepository.findById(institutionId).get(),
                readEntries = true
            )
        )
        unprofessionalRepository.save(
            Unprofessional(
                id = UnprofessionalKey(employeeId = saved.id, sponsorId = sponsorId),
                employee = saved,
                sponsor = sponsorRepository.findById(sponsorId).get()
            )
        )
    }
}
