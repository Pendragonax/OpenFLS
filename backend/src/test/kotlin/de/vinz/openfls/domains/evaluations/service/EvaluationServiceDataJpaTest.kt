package de.vinz.openfls.domains.evaluations.service

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationYearResult
import de.vinz.openfls.domains.evaluations.entity.Evaluation
import de.vinz.openfls.domains.evaluations.repository.EvaluationRepository
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.repository.GoalRepository
import de.vinz.openfls.domains.goals.service.GoalService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

@DataJpaTest
@Import(EvaluationService::class, TestBeans::class)
class EvaluationServiceDataJpaTest {

    @Autowired
    lateinit var evaluationService: EvaluationService

    @Autowired
    lateinit var evaluationRepository: EvaluationRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var goalRepository: GoalRepository

    @MockitoBean
    lateinit var goalService: GoalService

    @MockitoBean
    lateinit var assistancePlanService: AssistancePlanService

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var accessService: AccessService

    private lateinit var employee: Employee
    private lateinit var goal: Goal

    @BeforeEach
    fun setUp() {
        employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        goal = goalRepository.save(Goal(title = "Ziel"))
        whenever(accessService.getId()).thenReturn(employee.id!!)
        whenever(employeeService.getById(employee.id!!)).thenReturn(employee)
        whenever(accessService.canWriteEntries(0)).thenReturn(true)
        whenever(accessService.canReadEntries(0)).thenReturn(true)
        whenever(goalService.getEntityById(goal.id)).thenReturn(goal)
    }

    @Test
    fun create_validRequest_persistsEntityWithGoalAndCreator() {
        // When
        val result = evaluationService.create(
            EvaluationCreateRequest(goalId = goal.id, date = LocalDate.of(2026, 2, 1), content = "Test", approved = true)
        )

        // Then
        val response = (result as EvaluationCreateResult.Success).response
        val saved = evaluationRepository.findById(response.id).get()
        assertThat(saved.content).isEqualTo("Test")
        assertThat(saved.approved).isTrue()
        assertThat(saved.goal?.id).isEqualTo(goal.id)
        assertThat(saved.createdBy?.id).isEqualTo(employee.id)
    }

    @Test
    fun update_existingEntity_updatesFields() {
        // Given
        val existing = evaluationRepository.save(Evaluation(
            date = LocalDate.of(2026, 1, 1),
            content = "Old",
            approved = false,
            createdBy = employee,
            updatedBy = employee,
            goal = goal
        ))

        // When
        val result = evaluationService.update(
            EvaluationUpdateRequest(id = existing.id, date = LocalDate.of(2026, 2, 2), content = "New", approved = true)
        )

        // Then
        assertThat(result).isInstanceOf(EvaluationUpdateResult.Success::class.java)
        val saved = evaluationRepository.findById(existing.id).get()
        assertThat(saved.content).isEqualTo("New")
        assertThat(saved.approved).isTrue()
    }

    @Test
    fun getYearEvaluations_readsPersistedEvaluationsPerGoalAndMonth() {
        // Given
        val plan = AssistancePlan(
            id = 1L,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31)
        ).also { it.goals.add(goal) }
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(plan)
        evaluationRepository.save(Evaluation(
            date = LocalDate.of(2026, 4, 15),
            content = "April",
            createdBy = employee,
            updatedBy = employee,
            goal = goal
        ))

        // When
        val result = evaluationService.getYearEvaluationsByAssistancePlanIdAndYear(1L, 2026)

        // Then
        val months = (result as EvaluationYearResult.Success).response.values.single().months
        assertThat(months[3].evaluation?.content).isEqualTo("April")
        assertThat(months[3].evaluation?.createdBy).isEqualTo("Mustermann Max")
        assertThat(months[4].evaluation).isNull()
    }
}
