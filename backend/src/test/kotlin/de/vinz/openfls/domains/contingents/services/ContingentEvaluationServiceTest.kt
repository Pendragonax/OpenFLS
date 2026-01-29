package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.contingents.dtos.ContingentEvaluationDto
import de.vinz.openfls.domains.contingents.dtos.EmployeeContingentEvaluationDto
import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.domains.services.projections.ServiceProjection
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContingentEvaluationServiceTest {

    @Mock
    lateinit var contingentService: ContingentService

    @Mock
    lateinit var serviceService: ServiceService

    @InjectMocks
    lateinit var contingentEvaluationService: ContingentEvaluationService

    @Test
    fun testGenerateContingentEvaluationByYearAndInstitution_WhenMultipleYearlyContingents_ThenReturnMatchingEvaluation() {
        // Given
        val year = 2024
        val institutionId: Long = 1

        val institution = mockInstitution(1L, "Apple")
        val client = mockClient(1L, "Sie", "Client")
        val employee1 = mockEmployee(1L, "Hans", "Meiser")
        val employee2 = mockEmployee(2L, "Angela", "Merkel")
        val contingent1 = mockContingent(
                id = 1L,
                employee = employee1,
                institution = institution,
                start = LocalDate.of(year, 1, 1),
                end = LocalDate.of(year, 2, 29),
                weeklyHours = 7.0)
        val contingent2 = mockContingent(
                id = 2L,
                employee = employee1,
                institution = institution,
                start = LocalDate.of(year, 3, 1),
                end = null,
                weeklyHours = 7.0)
        val contingent3 = mockContingent(
                id = 3L,
                employee = employee2,
                institution = institution,
                start = LocalDate.of(year, 11, 2),
                end = null,
                weeklyHours = 7.0)
        val service1 = mockService(
                id = 1L,
                start = LocalDateTime.of(year, 9, 13, 10, 0),
                minutes = 60,
                institution = institution,
                employee = employee1,
                client = client)
        val service2 = mockService(
                id = 2L,
                start = LocalDateTime.of(year, 6, 13, 10, 0),
                minutes = 60,
                institution = institution,
                employee = employee2,
                client = client)

        val contingents = listOf(contingent1, contingent2, contingent3)
        val services = listOf(service1, service2)  // Mocked services

        whenever(contingentService.getAllByInstitutionAndYear(institutionId, year)).thenReturn(contingents)
        whenever(serviceService.getAllByInstitutionAndYear(institutionId, year)).thenReturn(services)

        val contingentHours1 = listOf(46.37, 31.0, 29.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        val contingentHours2 = listOf(237.44, 0.0, 0.0, 31.0, 30.0, 31.0, 30.0, 31.0, 31.0, 30.0, 31.0, 30.0, 31.0)
        val contingentHours3 = listOf(46.37, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 29.0, 31.0)

        whenever(contingentService.getContingentHoursByYear(year, contingent1)).thenReturn(contingentHours1)
        whenever(contingentService.getContingentHoursByYear(year, contingent2)).thenReturn(contingentHours2)
        whenever(contingentService.getContingentHoursByYear(year, contingent3)).thenReturn(contingentHours3)

        // When
        val evaluation: ContingentEvaluationDto = contingentEvaluationService.generateContingentEvaluationFor(year, institutionId)

        // Then
        assertThat(evaluation).isNotNull
        assertThat(evaluation.employees).hasSize(2)

        val evaluations = evaluation.employees
        assertThat(evaluations[0].employeeId).isEqualTo(1L)
        assertThat(evaluations[1].employeeId).isEqualTo(2L)
        assertThat(evaluations[0].contingentHours[0]).isEqualTo(284.21)
        assertThat(evaluations[1].contingentHours[0]).isEqualTo(46.37)
        assertThat(evaluations[0].executedHours[0]).isEqualTo(1.0)
        assertThat(evaluations[1].executedHours[0]).isEqualTo(1.0)
        assertThat(evaluations[0].executedPercent[0]).isEqualTo(0.35)
        assertThat(evaluations[1].executedPercent[0]).isEqualTo(2.15)
        assertThat(evaluations[0].summedExecutedPercent[0]).isEqualTo(0.35)
        assertThat(evaluations[0].summedExecutedPercent[9]).isEqualTo(0.47)
        assertThat(evaluations[0].summedExecutedPercent[12]).isEqualTo(0.35)
        assertThat(evaluations[1].summedExecutedPercent[0]).isEqualTo(2.15)
        assertThat(evaluations[1].summedExecutedPercent[11]).isEqualTo(4.44)
        assertThat(evaluations[1].summedExecutedPercent[12]).isEqualTo(2.15)
        assertThat(evaluations[0].missingHours[0]).isEqualTo(283.21)
        assertThat(evaluations[1].missingHours[0]).isEqualTo(45.37)
    }

    @Test
    fun testGetEmployeeContingentEvaluations_WhenMultipleYearlyContingents_ThenReturnMatchingEvaluation() {
        // Given
        val year = 2024
        val institutionId: Long = 1

        val institution = mockInstitution(1L, "Apple")
        val client = mockClient(1L, "Sie", "Client")
        val employee1 = mockEmployee(1L, "Hans", "Meiser")
        val employee2 = mockEmployee(2L, "Angela", "Merkel")
        val contingent1 = mockContingent(
                id = 1L,
                start = LocalDate.of(year, 1, 1),
                end = LocalDate.of(year, 2, 29),
                weeklyHours = 7.0,
                employee = employee1,
                institution = institution)
        val contingent2 = mockContingent(
                id = 1L,
                start = LocalDate.of(year, 3, 1),
                end = null,
                weeklyHours = 7.0,
                employee = employee1,
                institution = institution)
        val contingent3 = mockContingent(
                id = 1L,
                start = LocalDate.of(year, 11, 2),
                end = null,
                weeklyHours = 7.0,
                employee = employee2,
                institution = institution)
        val service1 = mockService(
                id = 1L,
                start = LocalDateTime.of(year, 9, 13, 10, 0),
                minutes = 60,
                institution = institution,
                employee = employee1,
                client = client)
        val service2 = mockService(
                id = 2L,
                start = LocalDateTime.of(year, 6, 13, 10, 0),
                minutes = 60,
                institution = institution,
                employee = employee2,
                client = client)

        val contingents = listOf(contingent1, contingent2, contingent3)
        val services = listOf(service1, service2)

        whenever(contingentService.getAllByInstitutionAndYear(institutionId, year)).thenReturn(contingents)
        whenever(serviceService.getAllByInstitutionAndYear(institutionId, year)).thenReturn(services)

        val contingentHours1 = listOf(46.37, 31.0, 29.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        val contingentHours2 = listOf(237.44, 0.0, 0.0, 31.0, 30.0, 31.0, 30.0, 31.0, 31.0, 30.0, 31.0, 30.0, 31.0)
        val contingentHours3 = listOf(46.37, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 29.0, 31.0)

        whenever(contingentService.getContingentHoursByYear(year, contingent1)).thenReturn(contingentHours1)
        whenever(contingentService.getContingentHoursByYear(year, contingent2)).thenReturn(contingentHours2)
        whenever(contingentService.getContingentHoursByYear(year, contingent3)).thenReturn(contingentHours3)

        // When
        val evaluations: List<EmployeeContingentEvaluationDto> =
                contingentEvaluationService.getEmployeeContingentEvaluations(year, contingents, services)

        // Then
        assertThat(evaluations).isNotNull
        assertThat(evaluations).hasSize(2)
        assertThat(evaluations[0].employeeId).isEqualTo(1L)
        assertThat(evaluations[1].employeeId).isEqualTo(2L)
        assertThat(evaluations[0].contingentHours[0]).isEqualTo(284.21)
        assertThat(evaluations[1].contingentHours[0]).isEqualTo(46.37)
        assertThat(evaluations[0].executedHours[0]).isEqualTo(1.0)
        assertThat(evaluations[1].executedHours[0]).isEqualTo(1.0)
        assertThat(evaluations[0].executedPercent[0]).isEqualTo(0.35)
        assertThat(evaluations[1].executedPercent[0]).isEqualTo(2.15)
        assertThat(evaluations[0].summedExecutedPercent[0]).isEqualTo(0.35)
        assertThat(evaluations[0].summedExecutedPercent[9]).isEqualTo(0.47)
        assertThat(evaluations[0].summedExecutedPercent[12]).isEqualTo(0.35)
        assertThat(evaluations[1].summedExecutedPercent[0]).isEqualTo(2.15)
        assertThat(evaluations[1].summedExecutedPercent[11]).isEqualTo(4.44)
        assertThat(evaluations[1].summedExecutedPercent[12]).isEqualTo(2.15)
        assertThat(evaluations[0].missingHours[0]).isEqualTo(283.21)
        assertThat(evaluations[1].missingHours[0]).isEqualTo(45.37)
    }

    @Test
    fun testGetExecutedHoursByYearAndEmployee_WhenServicesExist_ThenReturnExecutedHours() {
        // Given
        val year = 2024
        val employeeId: Long = 1
        val service = mock(ServiceProjection::class.java)
        val employee = mock(EmployeeSoloProjection::class.java)
        val start = LocalDateTime.of(year, 1, 1, 1, 1, 1)
        whenever(employee.id).thenReturn(employeeId)
        whenever(service.employee).thenReturn(employee)
        whenever(service.start).thenReturn(start)
        whenever(service.minutes).thenReturn(60)

        val services = listOf(service)

        // When
        val executedHours: List<Double> = contingentEvaluationService.getExecutedHoursByYearAndEmployee(year, employeeId, services)

        // Then
        assertThat(executedHours[1]).isEqualTo(TimeDoubleService.convertMinutesToTimeDouble(60))
        assertThat(executedHours[0]).isEqualTo(TimeDoubleService.convertMinutesToTimeDouble(60))
    }

    @Test
    fun testGetMissingHours_WhenExecutedLessThenContingent_ThenReturnPositiveMissingHours() {
        // Given
        val contingentHours = listOf(10.0, 20.0)
        val executedHours = listOf(5.0, 15.0)

        // When
        val missingHours: List<Double> = contingentEvaluationService.getMissingHours(contingentHours, executedHours)

        // Then
        assertThat(missingHours).containsExactly(5.0, 5.0)
    }

    @Test
    fun testGetExecutedPercent_GivenExecutedLessThenContingent_ThenReturnCorrectPercent() {
        // Given
        val contingentHours = listOf(10.0, 20.0)
        val executedHours = listOf(5.0, 15.0)

        // When
        val executedPercent: List<Double> = contingentEvaluationService.getExecutedPercent(contingentHours, executedHours)

        // Then
        assertThat(executedPercent).containsExactly(50.0, 75.0)
    }

    @Test
    fun testGetSummedExecutedPercent_GivenSummedExecutedLessThenContingent_ThenReturnCorrectSummedPercent() {
        // Given
        val contingentHours = listOf(90.0, 10.0, 20.0, 15.0, 30.0, 15.0)
        val executedHours = listOf(30.0, 5.0, 15.0, 0.0, 0.0, 10.0)

        // When
        val summedExecutedPercent: List<Double> =
                contingentEvaluationService.getSummedExecutedPercent(contingentHours, executedHours)

        // Then
        assertThat(summedExecutedPercent).containsExactly(33.33, 64.35, 85.84, 57.22, 34.32, 42.91)
    }

    @Test
    fun testGetSummedExecutedPercent_GivenContingentAndExecutedHours_ThenReturnCorrectSummedPercent() {
        // Given
        val contingentHours = listOf(284.21, 31.0, 29.0, 31.0, 30.0, 31.0, 30.0, 31.0, 31.0, 30.0, 31.0, 30.0, 31.0)
        val executedHours = listOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        // When
        val summedExecutedPercent: List<Double> =
                contingentEvaluationService.getSummedExecutedPercent(contingentHours, executedHours)

        // Then
        assertThat(summedExecutedPercent).containsExactly(0.35, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6, 0.53, 0.47, 0.42, 0.38, 0.35)
    }

    @Test
    fun testCalculatePercent_GivenContingentZero_ThenReturnZero() {
        // When
        val percent = contingentEvaluationService.getExecutedPercent(listOf(0.0), listOf(10.0))

        // Then
        assertThat(percent[0]).isEqualTo(0.0)
    }


    // Helper methods
    private fun mockInstitution(id: Long, name: String): InstitutionSoloProjection {
        val institution = mock(InstitutionSoloProjection::class.java)
        whenever(institution.id).thenReturn(id)
        whenever(institution.name).thenReturn(name)
        return institution
    }

    private fun mockClient(id: Long, firstName: String, lastName: String): ClientSoloProjection {
        val client = mock(ClientSoloProjection::class.java)
        whenever(client.id).thenReturn(id)
        whenever(client.firstName).thenReturn(firstName)
        whenever(client.lastName).thenReturn(lastName)
        return client
    }

    private fun mockEmployee(id: Long, firstName: String, lastName: String): EmployeeSoloProjection {
        val employee = mock(EmployeeSoloProjection::class.java)
        whenever(employee.id).thenReturn(id)
        whenever(employee.firstname).thenReturn(firstName)
        whenever(employee.lastname).thenReturn(lastName)
        return employee
    }

    private fun mockContingent(
            employee: EmployeeSoloProjection,
            institution: InstitutionSoloProjection,
            start: LocalDate,
            end: LocalDate?,
            weeklyHours: Double,
            id: Long
    ): ContingentProjection {
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.id).thenReturn(id)
        whenever(contingent.employee).thenReturn(employee)
        whenever(contingent.institution).thenReturn(institution)
        whenever(contingent.start).thenReturn(start)
        whenever(contingent.end).thenReturn(end)
        whenever(contingent.weeklyServiceHours).thenReturn(weeklyHours)
        return contingent
    }

    private fun mockService(
            id: Long,
            employee: EmployeeSoloProjection,
            institution: InstitutionSoloProjection,
            client: ClientSoloProjection,
            start: LocalDateTime,
            minutes: Int
    ): ServiceProjection {
        val service = mock(ServiceProjection::class.java)
        whenever(service.id).thenReturn(id)
        whenever(service.employee).thenReturn(employee)
        whenever(service.institution).thenReturn(institution)
        whenever(service.client).thenReturn(client)
        whenever(service.start).thenReturn(start)
        whenever(service.minutes).thenReturn(minutes)
        return service
    }
}
