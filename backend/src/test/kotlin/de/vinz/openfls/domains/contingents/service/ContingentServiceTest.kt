package de.vinz.openfls.domains.contingents.service

import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.contingents.repository.ContingentRepository
import de.vinz.openfls.domains.contingents.dto.ContingentCreateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentCreateResult
import de.vinz.openfls.domains.contingents.dto.ContingentDeleteResult
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateResult
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.permissions.service.AccessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ContingentServiceTest {

    @Mock
    lateinit var contingentRepository: ContingentRepository

    @Mock
    lateinit var institutionService: InstitutionService

    @Mock
    lateinit var employeeService: EmployeeService

    @Mock
    lateinit var accessService: AccessService

    private lateinit var contingentService: ContingentService

    @BeforeEach
    fun setUp() {
        contingentService = ContingentService(
            contingentRepository,
            institutionService,
            employeeService,
            accessService
        )
    }

    @Test
    fun create_validDto_returnsSavedDto() {
        // Given
        val dto = createRequest(
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 12, 31),
            weeklyHours = 10.0,
            employeeId = 5,
            institutionId = 7
        )
        val employee = Employee(id = 5)
        val institution = Institution(id = 7)
        val saved = Contingent(
            id = 12,
            start = dto.start,
            end = dto.end,
            weeklyServiceHours = dto.weeklyServiceHours,
            employee = employee,
            institution = institution
        )
        whenever(employeeService.getById(dto.employeeId)).thenReturn(employee)
        whenever(institutionService.getEntityById(dto.institutionId)).thenReturn(institution)
        whenever(contingentRepository.save(any<Contingent>())).thenReturn(saved)

        // When
        val result = contingentService.create(dto) as ContingentCreateResult.Success

        // Then
        assertThat(result.response.id).isEqualTo(12)
        assertThat(result.response.employeeId).isEqualTo(5)
        assertThat(result.response.institutionId).isEqualTo(7)
    }

    @Test
    fun create_archivedEmployee_returnsEmployeeArchived() {
        // Given
        val dto = createRequest(
            start = LocalDate.of(2024, 1, 1),
            weeklyHours = 10.0,
            employeeId = 5,
            institutionId = 7
        )
        val archivedEmployee = Employee(id = 5, archived = true)
        whenever(employeeService.getById(dto.employeeId)).thenReturn(archivedEmployee)

        // When
        val result = contingentService.create(dto)

        // Then
        assertThat(result).isEqualTo(ContingentCreateResult.EmployeeArchived("employee is archived"))
    }

    @Test
    fun create_endBeforeStart_returnsInvalidRange() {
        // Given
        val dto = createRequest(
            start = LocalDate.of(2024, 2, 1),
            end = LocalDate.of(2024, 1, 1),
            weeklyHours = 10.0,
            employeeId = 5,
            institutionId = 7
        )

        // When
        val result = contingentService.create(dto)

        // Then
        assertThat(result).isEqualTo(ContingentCreateResult.InvalidRange("end before start"))
    }

    @Test
    fun update_notExisting_returnsNotFound() {
        // Given
        val dto = updateRequest(id = 9)
        whenever(contingentRepository.findById(dto.id)).thenReturn(Optional.empty())

        // When
        val result = contingentService.update(dto)

        // Then
        assertThat(result).isEqualTo(ContingentUpdateResult.NotFound)
    }

    @Test
    fun update_endBeforeStart_returnsInvalidRange() {
        // Given
        val dto = updateRequest(
            id = 9,
            start = LocalDate.of(2024, 3, 1),
            end = LocalDate.of(2024, 2, 1)
        )
        whenever(contingentRepository.findById(dto.id)).thenReturn(Optional.of(Contingent(id = 9)))

        // When
        val result = contingentService.update(dto)

        // Then
        assertThat(result).isEqualTo(ContingentUpdateResult.InvalidRange("end before start"))
    }

    @Test
    fun update_validDto_returnsSavedDto() {
        // Given
        val dto = updateRequest(
            id = 9,
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 2, 1),
            weeklyHours = 5.0,
            employeeId = 2,
            institutionId = 3
        )
        val saved = Contingent(
            id = 9,
            start = dto.start,
            end = dto.end,
            weeklyServiceHours = dto.weeklyServiceHours,
            employee = Employee(id = 2),
            institution = Institution(id = 3)
        )
        whenever(contingentRepository.findById(dto.id)).thenReturn(Optional.of(Contingent(id = 9)))
        whenever(employeeService.getById(dto.employeeId)).thenReturn(Employee(id = dto.employeeId))
        whenever(institutionService.getEntityById(dto.institutionId)).thenReturn(Institution(id = dto.institutionId))
        whenever(contingentRepository.save(any<Contingent>())).thenReturn(saved)

        // When
        val result = contingentService.update(dto) as ContingentUpdateResult.Success

        // Then
        assertThat(result.response.id).isEqualTo(9)
        assertThat(result.response.employeeId).isEqualTo(2)
        assertThat(result.response.institutionId).isEqualTo(3)
        verify(employeeService).getById(dto.employeeId)
        verify(institutionService).getEntityById(dto.institutionId)
    }

    @Test
    fun update_archivedEmployee_returnsEmployeeArchived() {
        // Given
        val dto = updateRequest(
            id = 9,
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 2, 1),
            weeklyHours = 5.0,
            employeeId = 2,
            institutionId = 3
        )
        whenever(contingentRepository.findById(dto.id)).thenReturn(Optional.of(Contingent(id = 9)))
        whenever(employeeService.getById(dto.employeeId)).thenReturn(Employee(id = dto.employeeId, archived = true))

        // When
        val result = contingentService.update(dto)

        // Then
        assertThat(result).isEqualTo(ContingentUpdateResult.EmployeeArchived("employee is archived"))
    }

    @Test
    fun delete_existingContingent_deletesById() {
        // Given
        whenever(contingentRepository.findById(11)).thenReturn(Optional.of(Contingent(id = 11)))

        // When
        val result = contingentService.delete(11)

        // Then
        assertThat(result).isInstanceOf(ContingentDeleteResult.Success::class.java)
        verify(contingentRepository).deleteById(11)
    }

    @Test
    fun delete_missingContingent_returnsNotFound() {
        // Given
        whenever(contingentRepository.findById(11)).thenReturn(Optional.empty())

        // When
        val result = contingentService.delete(11)

        // Then
        assertThat(result).isEqualTo(ContingentDeleteResult.NotFound)
    }

    @Test
    fun getAll_unsortedEntities_returnsSortedByStart() {
        // Given
        val earlier = Contingent(id = 1, start = LocalDate.of(2024, 1, 1))
        val later = Contingent(id = 2, start = LocalDate.of(2024, 3, 1))
        whenever(contingentRepository.findAll()).thenReturn(listOf(later, earlier))

        // When
        val result = contingentService.getAll()

        // Then
        assertThat(result.map { it.id }).containsExactly(1, 2)
    }

    @Test
    fun getAllEntitiesByInstitutionAndYear_delegatesToRepository() {
        // Given
        val institutionId = 9L
        val year = 2024
        val contingent = Contingent(id = 1)
        whenever(contingentRepository.findByInstitutionIdAndStartAndEnd(
            institutionId,
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 12, 31)
        )).thenReturn(listOf(contingent))

        // When
        val result = contingentService.getAllEntitiesByInstitutionAndYear(institutionId, year)

        // Then
        assertThat(result).containsExactly(contingent)
    }

    @Test
    fun getById_existingEntity_returnsDto() {
        // Given
        val entity = Contingent(id = 4, start = LocalDate.of(2024, 1, 1))
        whenever(contingentRepository.findById(4)).thenReturn(Optional.of(entity))

        // When
        val result = contingentService.getById(4)

        // Then
        assertThat(result?.id).isEqualTo(4)
    }

    @Test
    fun getById_missingEntity_returnsNull() {
        // Given
        whenever(contingentRepository.findById(99)).thenReturn(Optional.empty())

        // When
        val result = contingentService.getById(99)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun getByEmployeeId_unsortedEntities_returnsSortedByEmployeeId() {
        // Given
        val contingent1 = Contingent(id = 1, employee = Employee(id = 2, archived = false))
        val contingent2 = Contingent(id = 2, employee = Employee(id = 1, archived = false))
        whenever(employeeService.getById(7)).thenReturn(Employee(id = 7, archived = false))
        whenever(contingentRepository.findAllByEmployeeId(7)).thenReturn(listOf(contingent1, contingent2))

        // When
        val result = contingentService.getByEmployeeId(7)

        // Then
        assertThat(result.map { it.employeeId }).containsExactly(1, 2)
    }

    @Test
    fun getByInstitutionId_unsortedEntities_returnsSortedByInstitutionId() {
        // Given
        val contingent1 = Contingent(id = 1, employee = Employee(id = 2, archived = false), institution = Institution(id = 2))
        val contingent2 = Contingent(id = 2, employee = Employee(id = 1, archived = false), institution = Institution(id = 1))
        whenever(contingentRepository.findAllByInstitutionId(3)).thenReturn(listOf(contingent1, contingent2))

        // When
        val result = contingentService.getByInstitutionId(3)

        // Then
        assertThat(result.map { it.institutionId }).containsExactly(1, 2)
    }

    @Test
    fun getByInstitutionId_defaultHidesArchivedEmployees() {
        // Given
        val activeEmployee = Employee(id = 1, archived = false)
        val archivedEmployee = Employee(id = 2, archived = true)
        val activeContingent = contingent(id = 1L, employee = activeEmployee, institution = Institution(id = 9))
        val archivedContingent = contingent(id = 2L, employee = archivedEmployee, institution = Institution(id = 9))
        whenever(contingentRepository.findAllByInstitutionId(9)).thenReturn(listOf(archivedContingent, activeContingent))

        // When
        val result = contingentService.getByInstitutionId(9)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo(1L)
    }

    @Test
    fun getByEmployeeId_archivedEmployee_returnsEmptyList() {
        // Given
        whenever(employeeService.getById(7)).thenReturn(Employee(id = 7, archived = true))

        // When
        val result = contingentService.getByEmployeeId(7)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun getByEmployeeId_archivedEmployee_withOptInReturnsSortedContingents() {
        // Given
        val contingent1 = Contingent(id = 1, employee = Employee(id = 2, archived = true), institution = Institution(id = 2))
        val contingent2 = Contingent(id = 2, employee = Employee(id = 1, archived = true), institution = Institution(id = 1))
        whenever(employeeService.getById(7)).thenReturn(Employee(id = 7, archived = true))
        whenever(contingentRepository.findAllByEmployeeId(7)).thenReturn(listOf(contingent1, contingent2))

        // When
        val result = contingentService.getByEmployeeId(7, includeArchivedEmployees = true)

        // Then
        assertThat(result.map { it.institutionId }).containsExactly(1, 2)
    }

    @Test
    fun canModifyContingent_adminUser_returnsTrue() {
        // Given
        whenever(accessService.isAdmin()).thenReturn(true)

        // When
        val result = contingentService.canModifyContingent(1)

        // Then
        assertThat(result).isTrue()
        verify(accessService).isAdmin()
        verifyNoMoreInteractions(accessService)
    }

    @Test
    fun canModifyContingent_leaderOfInstitution_returnsTrue() {
        // Given
        whenever(accessService.isAdmin()).thenReturn(false)
        whenever(accessService.getId()).thenReturn(7)
        whenever(contingentRepository.findById(6)).thenReturn(Optional.of(Contingent(id = 6, institution = Institution(id = 44))))
        whenever(accessService.isLeader(7, 44)).thenReturn(true)

        // When
        val result = contingentService.canModifyContingent(6)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun canModifyContingent_exception_returnsFalse() {
        // Given
        whenever(accessService.isAdmin()).thenThrow(RuntimeException("fail"))

        // When
        val result = contingentService.canModifyContingent(1)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun create_missingInstitution_returnsInstitutionNotFound() {
        // Given
        val dto = createRequest(employeeId = 5, institutionId = 7)
        whenever(employeeService.getById(dto.employeeId)).thenReturn(Employee(id = 5))
        whenever(institutionService.getEntityById(dto.institutionId)).thenReturn(null)

        // When
        val result = contingentService.create(dto)

        // Then
        assertThat(result).isEqualTo(ContingentCreateResult.InstitutionNotFound("institution not found"))
    }

    @Test
    fun create_missingEmployee_returnsEmployeeNotFound() {
        // Given
        val dto = createRequest(employeeId = 5, institutionId = 7)
        whenever(employeeService.getById(dto.employeeId)).thenReturn(null)

        // When
        val result = contingentService.create(dto)

        // Then
        assertThat(result).isEqualTo(ContingentCreateResult.EmployeeNotFound("employee not found"))
    }

    @Test
    fun getAllEntitiesByEmployeeId_activeEmployee_returnsEntities() {
        // Given
        val contingent = Contingent(id = 1, employee = Employee(id = 7))
        whenever(employeeService.getById(7)).thenReturn(Employee(id = 7, archived = false))
        whenever(contingentRepository.findAllByEmployeeId(7)).thenReturn(listOf(contingent))

        // When
        val result = contingentService.getAllEntitiesByEmployeeId(7)

        // Then
        assertThat(result).containsExactly(contingent)
    }

    private fun createRequest(
        start: LocalDate = LocalDate.of(2024, 1, 1),
        end: LocalDate? = null,
        weeklyHours: Double = 7.0,
        employeeId: Long = 1,
        institutionId: Long = 1
    ) = ContingentCreateRequest(
        start = start,
        end = end,
        weeklyServiceHours = weeklyHours,
        employeeId = employeeId,
        institutionId = institutionId
    )

    private fun updateRequest(
        id: Long = 0,
        start: LocalDate = LocalDate.of(2024, 1, 1),
        end: LocalDate? = null,
        weeklyHours: Double = 7.0,
        employeeId: Long = 1,
        institutionId: Long = 1
    ) = ContingentUpdateRequest(
        id = id,
        start = start,
        end = end,
        weeklyServiceHours = weeklyHours,
        employeeId = employeeId,
        institutionId = institutionId
    )

    private fun contingent(
        id: Long,
        employee: Employee,
        institution: Institution
    ): Contingent {
        return Contingent(
            id = id,
            start = LocalDate.of(2024, 1, 1),
            weeklyServiceHours = 7.0,
            employee = employee,
            institution = institution
        )
    }
}
