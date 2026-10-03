package de.vinz.openfls.domains.contingents.service

import de.vinz.openfls.domains.absence.service.AbsenceService
import de.vinz.openfls.domains.absence.entity.Absence
import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.contingents.dto.ContingentEvaluationResponse
import de.vinz.openfls.domains.contingents.dto.ContingentServiceEntryDto
import de.vinz.openfls.domains.contingents.dto.EmployeeContingentEvaluationResponse
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.services.dto.ContingentEvaluationServiceDto
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.lenient
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ContingentEvaluationServiceTest {

    @Mock
    lateinit var contingentService: ContingentService

    @Mock
    lateinit var contingentCalculationService: ContingentCalculationService

    @Mock
    lateinit var serviceService: ServiceService

    @Mock
    lateinit var absenceService: AbsenceService

    @InjectMocks
    lateinit var contingentEvaluationService: ContingentEvaluationService

    @Test
    fun generateContingentEvaluationFor_singleEmployeeSingleContingent_returnsExpectedTotals() {
        // Given
        val year = 2024
        val institutionId = 1L
        val institution = mockInstitution()
        val employee = mockEmployee(10L, "Hans", "Meiser")
        val contingent = mockContingent(
            id = 1L,
            employee = employee,
            institution = institution,
            start = LocalDate.of(year, 1, 1),
            end = null,
            weeklyHours = 7.0
        )
        val service = mockService(
            id = 21L,
            start = LocalDateTime.of(year, 1, 5, 10, 0),
            minutes = 60,
            employeeId = employee.id!!
        )
        val yearlyAbsences = emptyList<Absence>()
        val contingentHours = listOf(2.0, 2.0) + List(11) { 0.0 }

        whenever(serviceService.getContingentEvaluationServicesByInstitutionIdAndYear(institutionId, year)).thenReturn(listOf(service))
        whenever(contingentService.getAllEntitiesByInstitutionAndYear(institutionId, year)).thenReturn(listOf(contingent))
        whenever(absenceService.getAllEntitiesByYear(year)).thenReturn(yearlyAbsences)
        whenever(contingentCalculationService.calculateContingentHoursBy(year, contingent, yearlyAbsences))
            .thenReturn(contingentHours)

        // When
        val evaluation: ContingentEvaluationResponse =
            contingentEvaluationService.generateContingentEvaluationFor(year, institutionId)

        // Then
        assertThat(evaluation.institutionId).isEqualTo(institutionId)
        assertThat(evaluation.employees).hasSize(1)
        val employeeEvaluation = evaluation.employees.first()
        assertThat(employeeEvaluation.employeeId).isEqualTo(employee.id)
        assertThat(employeeEvaluation.archived).isFalse()
        assertThat(employeeEvaluation.executedHours[0]).isEqualTo(1.0)
        assertThat(employeeEvaluation.executedPercent[0]).isEqualTo(50.0)
        assertThat(employeeEvaluation.missingHours[0]).isEqualTo(1.0)
    }

    @Test
    fun getEmployeeContingentEvaluations_twoContingentsForSameEmployee_mergesContingentHours() {
        // Given
        val year = 2024
        val institution = mockInstitution()
        val employee = mockEmployee(2L, "Anna", "Alpha")
        val contingentOne = mockContingent(
            id = 1L,
            employee = employee,
            institution = institution,
            start = LocalDate.of(year, 1, 1),
            end = LocalDate.of(year, 6, 30),
            weeklyHours = 7.0
        )
        val contingentTwo = mockContingent(
            id = 2L,
            employee = employee,
            institution = institution,
            start = LocalDate.of(year, 7, 1),
            end = null,
            weeklyHours = 7.0
        )
        val service = serviceEntry(
            start = LocalDateTime.of(year, 1, 10, 9, 0),
            minutes = 60,
            employeeId = employee.id!!
        )
        val yearlyAbsences = emptyList<Absence>()
        val contingentHoursOne = listOf(1.0, 1.0) + List(11) { 0.0 }
        val contingentHoursTwo = listOf(2.0, 1.0, 1.0) + List(10) { 0.0 }

        whenever(contingentCalculationService.calculateContingentHoursBy(year, contingentOne, yearlyAbsences))
            .thenReturn(contingentHoursOne)
        whenever(contingentCalculationService.calculateContingentHoursBy(year, contingentTwo, yearlyAbsences))
            .thenReturn(contingentHoursTwo)

        // When
        val evaluations: List<EmployeeContingentEvaluationResponse> =
            contingentEvaluationService.getEmployeeContingentEvaluations(
                year,
                listOf(contingentOne, contingentTwo),
                listOf(service),
                yearlyAbsences
            )

        // Then
        assertThat(evaluations).hasSize(1)
        val evaluation = evaluations.first()
        assertThat(evaluation.employeeId).isEqualTo(employee.id)
        assertThat(evaluation.contingentHours[0]).isEqualTo(3.0)
        assertThat(evaluation.contingentHours[1]).isEqualTo(2.0)
        assertThat(evaluation.executedHours[0]).isEqualTo(1.0)
        assertThat(evaluation.executedPercent[0]).isEqualTo(33.33)
    }

    @Test
    fun getEmployeeContingentEvaluations_multipleEmployees_sortedByLastname() {
        // Given
        val year = 2024
        val institution = mockInstitution()
        val employeeA = mockEmployee(1L, "Anna", "Alpha")
        val employeeB = mockEmployee(2L, "Zoe", "Zulu")
        val contingentA = mockContingent(
            id = 1L,
            employee = employeeA,
            institution = institution,
            start = LocalDate.of(year, 1, 1),
            end = null,
            weeklyHours = 7.0
        )
        val contingentB = mockContingent(
            id = 2L,
            employee = employeeB,
            institution = institution,
            start = LocalDate.of(year, 1, 1),
            end = null,
            weeklyHours = 7.0
        )
        val yearlyAbsences = emptyList<Absence>()
        val contingentHours = listOf(1.0, 1.0) + List(11) { 0.0 }

        whenever(contingentCalculationService.calculateContingentHoursBy(year, contingentA, yearlyAbsences))
            .thenReturn(contingentHours)
        whenever(contingentCalculationService.calculateContingentHoursBy(year, contingentB, yearlyAbsences))
            .thenReturn(contingentHours)

        // When
        val evaluations = contingentEvaluationService.getEmployeeContingentEvaluations(
            year,
            listOf(contingentB, contingentA),
            emptyList(),
            yearlyAbsences
        )

        // Then
        assertThat(evaluations).hasSize(2)
        assertThat(evaluations[0].employeeId).isEqualTo(employeeA.id)
        assertThat(evaluations[1].employeeId).isEqualTo(employeeB.id)
    }

    @Test
    fun generateContingentEvaluationFor_archivedEmployee_marksArchivedEmployee() {
        // Given
        val year = 2024
        val institutionId = 1L
        val institution = mockInstitution()
        val archivedEmployee = mockEmployee(11L, "Anna", "Archive", archived = true)
        val archivedContingent = mockContingent(
            id = 2L,
            employee = archivedEmployee,
            institution = institution,
            start = LocalDate.of(year, 1, 1),
            end = null,
            weeklyHours = 7.0
        )
        val yearlyAbsences = emptyList<Absence>()
        val contingentHours = listOf(2.0, 2.0) + List(11) { 0.0 }

        whenever(serviceService.getContingentEvaluationServicesByInstitutionIdAndYear(institutionId, year)).thenReturn(emptyList())
        whenever(contingentService.getAllEntitiesByInstitutionAndYear(institutionId, year)).thenReturn(listOf(archivedContingent))
        whenever(absenceService.getAllEntitiesByYear(year)).thenReturn(yearlyAbsences)
        whenever(contingentCalculationService.calculateContingentHoursBy(year, archivedContingent, yearlyAbsences))
            .thenReturn(contingentHours)

        // When
        val evaluation = contingentEvaluationService.generateContingentEvaluationFor(year, institutionId, includeArchivedEmployees = true)

        // Then
        assertThat(evaluation.employees).hasSize(1)
        assertThat(evaluation.employees.first().archived).isTrue()
    }

    @Test
    fun generateContingentEvaluationFor_archivedEmployee_isExcludedByDefault() {
        // Given
        val year = 2024
        val institutionId = 1L
        val institution = mockInstitution()
        val activeEmployee = mockEmployee(10L, "Hans", "Meiser")
        val archivedEmployee = mockEmployee(11L, "Anna", "Archive", archived = true)
        val activeContingent = mockContingent(
            id = 1L,
            employee = activeEmployee,
            institution = institution,
            start = LocalDate.of(year, 1, 1),
            end = null,
            weeklyHours = 7.0
        )
        val archivedContingent = mockContingent(
            id = 2L,
            employee = archivedEmployee,
            institution = institution,
            start = LocalDate.of(year, 1, 1),
            end = null,
            weeklyHours = 7.0
        )
        val service = mockService(
            id = 21L,
            start = LocalDateTime.of(year, 1, 5, 10, 0),
            minutes = 60,
            employeeId = activeEmployee.id!!
        )
        val yearlyAbsences = emptyList<Absence>()
        val contingentHours = listOf(2.0, 2.0) + List(11) { 0.0 }

        whenever(serviceService.getContingentEvaluationServicesByInstitutionIdAndYear(institutionId, year)).thenReturn(listOf(service))
        whenever(contingentService.getAllEntitiesByInstitutionAndYear(institutionId, year)).thenReturn(listOf(activeContingent, archivedContingent))
        whenever(absenceService.getAllEntitiesByYear(year)).thenReturn(yearlyAbsences)
        whenever(contingentCalculationService.calculateContingentHoursBy(year, activeContingent, yearlyAbsences))
            .thenReturn(contingentHours)

        // When
        val evaluation = contingentEvaluationService.generateContingentEvaluationFor(year, institutionId)

        // Then
        assertThat(evaluation.employees).hasSize(1)
        assertThat(evaluation.employees.first().employeeId).isEqualTo(activeEmployee.id)
    }

    @Test
    fun getExecutedHoursByYearAndEmployee_servicesAcrossMonthsAndAbsence_skipsAbsentServices() {
        // Given
        val year = 2024
        val employeeId = 7L
        val otherEmployeeId = 9L
        val janService = serviceEntry(LocalDateTime.of(year, 1, 5, 8, 0), 0, employeeId)
        val febService = serviceEntry(LocalDateTime.of(year, 2, 6, 8, 0), 30, employeeId)
        val otherService = serviceEntry(LocalDateTime.of(year, 1, 5, 8, 0), 0, otherEmployeeId)
        val yearlyAbsences = listOf(Absence(absenceDate = LocalDate.of(year, 1, 5), employeeId = employeeId))

        // When
        val executedHours = contingentEvaluationService.getExecutedHoursByYearAndEmployee(
            year,
            employeeId,
            listOf(janService, febService, otherService),
            yearlyAbsences
        )

        // Then
        assertThat(executedHours[0]).isEqualTo(TimeDoubleService.convertMinutesToTimeDouble(30))
        assertThat(executedHours[1]).isEqualTo(0.0)
        assertThat(executedHours[2]).isEqualTo(TimeDoubleService.convertMinutesToTimeDouble(30))
    }

    @Test
    fun getMissingHours_contingentZeroOrNegative_returnsZero() {
        // Given
        val contingentHours = listOf(0.0, -1.0, 2.0)
        val executedHours = listOf(1.0, 1.0, 0.0)

        // When
        val missingHours = contingentEvaluationService.getMissingHours(contingentHours, executedHours)

        // Then
        assertThat(missingHours).containsExactly(0.0, 0.0, 2.0)
    }

    @Test
    fun getExecutedPercent_contingentZero_returnsZero() {
        // Given
        val contingentHours = listOf(0.0, 2.0)
        val executedHours = listOf(1.0, 1.0)

        // When
        val executedPercent = contingentEvaluationService.getExecutedPercent(contingentHours, executedHours)

        // Then
        assertThat(executedPercent).containsExactly(0.0, 50.0)
    }

    @Test
    fun getSummedExecutedPercent_constantRatio_returnsExpectedSummedValues() {
        // Given
        val contingentHours = listOf(2.0, 1.0, 1.0)
        val executedHours = listOf(1.0, 0.30, 0.30)

        // When
        val summedExecutedPercent = contingentEvaluationService.getSummedExecutedPercent(contingentHours, executedHours)

        // Then
        assertThat(summedExecutedPercent).containsExactly(50.0, 50.0, 50.0)
    }

    private fun mockInstitution(): Institution = Institution(id = 1)

    private fun mockEmployee(
        id: Long,
        firstName: String,
        lastName: String,
        archived: Boolean = false
    ): Employee = Employee(id = id, firstname = firstName, lastname = lastName, archived = archived)

    private fun mockContingent(
        id: Long,
        employee: Employee,
        institution: Institution,
        start: LocalDate,
        end: LocalDate?,
        weeklyHours: Double
    ): Contingent = Contingent(
        id = id,
        start = start,
        end = end,
        weeklyServiceHours = weeklyHours,
        employee = employee,
        institution = institution
    )

    private fun mockService(
        id: Long,
        start: LocalDateTime,
        minutes: Int,
        employeeId: Long
    ): ContingentEvaluationServiceDto {
        return ContingentEvaluationServiceDto(id = id, start = start, minutes = minutes, employeeId = employeeId)
    }

    private fun serviceEntry(start: LocalDateTime, minutes: Int, employeeId: Long): ContingentServiceEntryDto =
        ContingentServiceEntryDto(employeeId = employeeId, start = start, minutes = minutes)
}
