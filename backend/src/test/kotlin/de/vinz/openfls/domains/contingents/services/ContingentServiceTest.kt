package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.absence.dtos.EmployeeAbsenceResponseDTO
import de.vinz.openfls.domains.absence.dtos.YearAbsenceDTO
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.contingents.ContingentRepository
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
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
        val dto = contingentDto(
            id = 0,
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
        whenever(employeeService.getById(dto.employeeId, true)).thenReturn(employee)
        whenever(institutionService.getEntityById(dto.institutionId)).thenReturn(institution)
        whenever(contingentRepository.save(any())).thenReturn(saved)

        // When
        val result = contingentService.create(dto)

        // Then
        assertThat(result.id).isEqualTo(12)
        assertThat(result.employeeId).isEqualTo(5)
        assertThat(result.institutionId).isEqualTo(7)
    }

    @Test
    fun create_endBeforeStart_throwsIllegalArgument() {
        // Given
        val dto = contingentDto(
            id = 0,
            start = LocalDate.of(2024, 2, 1),
            end = LocalDate.of(2024, 1, 1),
            weeklyHours = 10.0,
            employeeId = 5,
            institutionId = 7
        )

        // When / Then
        assertThatThrownBy { contingentService.create(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("end before start")
    }

    @Test
    fun update_notExisting_throwsIllegalArgument() {
        // Given
        val dto = contingentDto(id = 9)
        whenever(contingentRepository.existsById(dto.id)).thenReturn(false)

        // When / Then
        assertThatThrownBy { contingentService.update(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("contingent not found")
    }

    @Test
    fun update_endBeforeStart_throwsIllegalArgument() {
        // Given
        val dto = contingentDto(
            id = 9,
            start = LocalDate.of(2024, 3, 1),
            end = LocalDate.of(2024, 2, 1)
        )
        whenever(contingentRepository.existsById(dto.id)).thenReturn(true)

        // When / Then
        assertThatThrownBy { contingentService.update(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("end before start")
    }

    @Test
    fun update_validDto_returnsSavedDto() {
        // Given
        val dto = contingentDto(
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
        whenever(contingentRepository.existsById(dto.id)).thenReturn(true)
        whenever(contingentRepository.save(any())).thenReturn(saved)

        // When
        val result = contingentService.update(dto)

        // Then
        assertThat(result.id).isEqualTo(9)
        assertThat(result.employeeId).isEqualTo(2)
        assertThat(result.institutionId).isEqualTo(3)
    }

    @Test
    fun delete_missingContingent_throwsIllegalArgument() {
        // Given
        whenever(contingentRepository.existsById(10)).thenReturn(false)

        // When / Then
        assertThatThrownBy { contingentService.delete(10) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("contingent not found")
    }

    @Test
    fun delete_existingContingent_deletesById() {
        // Given
        whenever(contingentRepository.existsById(11)).thenReturn(true)

        // When
        contingentService.delete(11)

        // Then
        verify(contingentRepository).deleteById(11)
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
    fun getAllByInstitutionAndYear_delegatesToRepository() {
        // Given
        val institutionId = 9L
        val year = 2024
        val projection = mock(ContingentProjection::class.java)
        whenever(contingentRepository.findByInstitutionIdAndStartAndEnd(
            institutionId,
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 12, 31)
        )).thenReturn(listOf(projection))

        // When
        val result = contingentService.getAllByInstitutionAndYear(institutionId, year)

        // Then
        assertThat(result).containsExactly(projection)
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
    fun getDtoById_missingEntity_returnsNull() {
        // Given
        whenever(contingentRepository.findById(99)).thenReturn(Optional.empty())

        // When
        val result = contingentService.getDtoById(99)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun existsById_existingId_returnsTrue() {
        // Given
        whenever(contingentRepository.existsById(5)).thenReturn(true)

        // When
        val result = contingentService.existsById(5)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun getByEmployeeId_unsortedEntities_returnsSortedByEmployeeId() {
        // Given
        val contingent1 = Contingent(id = 1, employee = Employee(id = 2))
        val contingent2 = Contingent(id = 2, employee = Employee(id = 1))
        whenever(contingentRepository.findAllByEmployeeId(7)).thenReturn(listOf(contingent1, contingent2))

        // When
        val result = contingentService.getByEmployeeId(7)

        // Then
        assertThat(result.map { it.employeeId }).containsExactly(1, 2)
    }

    @Test
    fun getByInstitutionId_unsortedEntities_returnsSortedByInstitutionId() {
        // Given
        val contingent1 = Contingent(id = 1, institution = Institution(id = 2))
        val contingent2 = Contingent(id = 2, institution = Institution(id = 1))
        whenever(contingentRepository.findAllByInstitutionId(3)).thenReturn(listOf(contingent1, contingent2))

        // When
        val result = contingentService.getByInstitutionId(3)

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
    fun calculateContingentHoursBy_yearWithAbsences_returnsExpectedHours() {
        // Given
        val year = 2024
        val contingent = mockContingentProjectionForHours(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 10),
            weeklyHours = 10.0
        )
        val absences = YearAbsenceDTO.of(
            year,
            listOf(EmployeeAbsenceResponseDTO(
                employeeId = contingent.employee.id,
                absenceDates = listOf(LocalDate.of(year, 1, 3))
            ))
        )
        val workdays = DateService.calculateWorkdaysInHesseBetween(contingent.start, contingent.end, year)
        val dailyHours = contingent.weeklyServiceHours / 5
        val expectedTotal = TimeDoubleService.convertDoubleToTimeDouble((workdays - 1) * dailyHours)

        // When
        val result = contingentService.calculateContingentHoursBy(year, contingent, absences)

        // Then
        assertThat(result[0]).isEqualTo(expectedTotal)
        assertThat(result[1]).isEqualTo(12.0)
    }

    @Test
    fun calculateContingentHoursBy_monthOutsideContingent_returnsZero() {
        // Given
        val year = 2024
        val contingent = mockContingentProjectionForRange(start = LocalDate.of(year, 3, 1))
        val absences = YearAbsenceDTO.of(year, emptyList())

        // When
        val result = contingentService.calculateContingentHoursBy(year, 1, contingent, absences)

        // Then
        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun calculateContingentHoursBy_monthWithAbsences_reducesHours() {
        // Given
        val year = 2024
        val contingent = mockContingentProjectionForHours(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 31),
            weeklyHours = 10.0
        )
        val absences = YearAbsenceDTO.of(
            year,
            listOf(EmployeeAbsenceResponseDTO(
                employeeId = contingent.employee.id,
                absenceDates = listOf(LocalDate.of(year, 1, 3))
            ))
        )
        val workdays = DateService.countWorkDaysOfMonthAndYearBetweenStartAndEnd(
            year,
            1,
            contingent.start,
            contingent.end!!
        )
        val expected = TimeDoubleService.convertDoubleToTimeDouble((workdays - 1) * (contingent.weeklyServiceHours / 5))

        // When
        val result = contingentService.calculateContingentHoursBy(year, 1, contingent, absences)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun countAbsenceDaysBy_filtersByEmployeeMonthAndContingent() {
        // Given
        val year = 2024
        val contingent = mockContingentProjectionForAbsences(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 31)
        )
        val absences = YearAbsenceDTO.of(
            year,
            listOf(
                EmployeeAbsenceResponseDTO(
                    employeeId = contingent.employee.id,
                    absenceDates = listOf(LocalDate.of(year, 1, 1), LocalDate.of(year, 2, 5))
                ),
                EmployeeAbsenceResponseDTO(
                    employeeId = contingent.employee.id + 1,
                    absenceDates = listOf(LocalDate.of(year, 1, 6))
                )
            )
        )

        // When
        val result = contingentService.countAbsenceDaysBy(year, 1, contingent, absences)

        // Then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun countAbsenceDaysInContingentForYear_countsOnlyInsideRange() {
        // Given
        val year = 2024
        val contingent = mockContingentProjectionForAbsences(
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 1, 10)
        )
        val absences = YearAbsenceDTO.of(
            year,
            listOf(EmployeeAbsenceResponseDTO(
                employeeId = contingent.employee.id,
                absenceDates = listOf(
                    LocalDate.of(year, 1, 2),
                    LocalDate.of(year, 1, 5),
                    LocalDate.of(year, 1, 11)
                )
            ))
        )

        // When
        val result = contingentService.countAbsenceDaysInContingentForYear(year, contingent, absences)

        // Then
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun isContingentInYearMonth_insideRange_returnsTrue() {
        // Given
        val contingent = mockContingentProjectionForRange(start = LocalDate.of(2024, 1, 1))
        whenever(contingent.end).thenReturn(LocalDate.of(2024, 3, 1))

        // When
        val result = contingentService.isContingentInYearMonth(2024, 2, contingent)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun isContingentInYearMonth_outsideRange_returnsFalse() {
        // Given
        val contingent = mockContingentProjectionForRange(start = LocalDate.of(2024, 3, 1))

        // When
        val result = contingentService.isContingentInYearMonth(2024, 2, contingent)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun calculateContingentMinutesForWorkdayBy_weekend_returnsZero() {
        // Given
        val date = LocalDate.of(2024, 1, 6)
        val contingents = listOf(contingentDto(start = LocalDate.of(2024, 1, 1)))

        // When
        val result = contingentService.calculateContingentMinutesForWorkdayBy(date, contingents)

        // Then
        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun calculateContingentMinutesForWorkdayBy_matchingContingent_returnsDailyMinutes() {
        // Given
        val date = LocalDate.of(2024, 1, 2)
        val contingents = listOf(contingentDto(
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 1, 31),
            weeklyHours = 10.0
        ))

        // When
        val result = contingentService.calculateContingentMinutesForWorkdayBy(date, contingents)

        // Then
        assertThat(result).isEqualTo(120.0)
    }

    @Test
    fun calculateContingentMinutesFor_rangeOfWorkdays_returnsCeiledMinutes() {
        // Given
        val start = LocalDate.of(2024, 1, 2)
        val end = LocalDate.of(2024, 1, 3)
        val contingents = listOf(contingentDto(
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 1, 31),
            weeklyHours = 10.0
        ))

        // When
        val result = contingentService.calculateContingentMinutesFor(start, end, contingents)

        // Then
        assertThat(result).isEqualTo(240)
    }

    private fun contingentDto(
        id: Long = 0,
        start: LocalDate = LocalDate.of(2024, 1, 1),
        end: LocalDate? = null,
        weeklyHours: Double = 7.0,
        employeeId: Long = 1,
        institutionId: Long = 1
    ): ContingentDto {
        val dto = ContingentDto()
        dto.id = id
        dto.start = start
        dto.end = end
        dto.weeklyServiceHours = weeklyHours
        dto.employeeId = employeeId
        dto.institutionId = institutionId
        return dto
    }

    private fun mockContingentProjectionForRange(start: LocalDate): ContingentProjection {
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(start)
        return contingent
    }

    private fun mockContingentProjectionForAbsences(
        start: LocalDate,
        end: LocalDate?,
        employeeId: Long = 1
    ): ContingentProjection {
        val employee = mock(EmployeeSoloProjection::class.java)
        val contingent = mock(ContingentProjection::class.java)
        whenever(employee.id).thenReturn(employeeId)
        whenever(contingent.employee).thenReturn(employee)
        whenever(contingent.start).thenReturn(start)
        whenever(contingent.end).thenReturn(end)
        return contingent
    }

    private fun mockContingentProjectionForHours(
        start: LocalDate,
        end: LocalDate?,
        weeklyHours: Double,
        employeeId: Long = 1
    ): ContingentProjection {
        val employee = mock(EmployeeSoloProjection::class.java)
        val contingent = mock(ContingentProjection::class.java)
        whenever(employee.id).thenReturn(employeeId)
        whenever(contingent.employee).thenReturn(employee)
        whenever(contingent.start).thenReturn(start)
        whenever(contingent.end).thenReturn(end)
        whenever(contingent.weeklyServiceHours).thenReturn(weeklyHours)
        return contingent
    }
}
