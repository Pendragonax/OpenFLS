package de.vinz.openfls.domains.evaluations.service

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationDeleteResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationYearResult
import de.vinz.openfls.domains.evaluations.entity.Evaluation
import de.vinz.openfls.domains.evaluations.repository.EvaluationRepository
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.service.GoalService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.permissions.service.AccessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.Optional

class EvaluationServiceTest {

    private val evaluationRepository: EvaluationRepository = mock()
    private val goalService: GoalService = mock()
    private val assistancePlanService: AssistancePlanService = mock()
    private val employeeService: EmployeeService = mock()
    private val accessService: AccessService = mock()
    private lateinit var service: EvaluationService

    private val institutionId = 5L
    private val employee = Employee(id = 3, firstname = "Max", lastname = "Muster")

    @BeforeEach
    fun setUp() {
        service = EvaluationService(evaluationRepository, goalService, assistancePlanService, employeeService, accessService)
        whenever(accessService.getId()).thenReturn(employee.id!!)
        whenever(employeeService.getEntityById(employee.id!!)).thenReturn(employee)
        whenever(accessService.canWriteEntries(institutionId)).thenReturn(true)
        whenever(accessService.canReadEntries(institutionId)).thenReturn(true)
        whenever(evaluationRepository.save(any<Evaluation>())).thenAnswer { it.arguments[0] }
    }

    private fun plan(archived: Boolean = false): AssistancePlan {
        return AssistancePlan(
            id = 1L,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 6, 30),
            institution = Institution(id = institutionId),
            client = Client(id = 9L, archived = archived)
        )
    }

    private fun goal(plan: AssistancePlan = plan(), id: Long = 11L, title: String = "Ziel"): Goal {
        return Goal(id = id, title = title, assistancePlan = plan).also { plan.goals.add(it) }
    }

    private fun evaluation(goal: Goal, id: Long = 100L, date: LocalDate = LocalDate.of(2026, 2, 10)): Evaluation {
        return Evaluation(id = id, date = date, content = "Inhalt", createdBy = employee, updatedBy = employee, goal = goal)
    }

    @Test
    fun create_unknownGoal_returnsGoalNotFound() {
        whenever(goalService.getEntityById(11L)).thenReturn(null)

        val result = service.create(EvaluationCreateRequest(goalId = 11L))

        assertThat(result).isEqualTo(EvaluationCreateResult.GoalNotFound)
        verify(evaluationRepository, never()).save(any<Evaluation>())
    }

    @Test
    fun create_withoutWriteAccess_returnsForbidden() {
        whenever(goalService.getEntityById(11L)).thenReturn(goal())
        whenever(accessService.canWriteEntries(institutionId)).thenReturn(false)

        val result = service.create(EvaluationCreateRequest(goalId = 11L))

        assertThat(result).isEqualTo(EvaluationCreateResult.Forbidden)
        verify(evaluationRepository, never()).save(any<Evaluation>())
    }

    @Test
    fun create_archivedClient_returnsClientArchived() {
        whenever(goalService.getEntityById(11L)).thenReturn(goal(plan(archived = true)))

        val result = service.create(EvaluationCreateRequest(goalId = 11L))

        assertThat(result).isEqualTo(EvaluationCreateResult.ClientArchived)
        verify(evaluationRepository, never()).save(any<Evaluation>())
    }

    @Test
    fun create_validRequest_savesWithGoalAndCurrentUser() {
        val goal = goal()
        whenever(goalService.getEntityById(11L)).thenReturn(goal)

        val result = service.create(
            EvaluationCreateRequest(goalId = 11L, date = LocalDate.of(2026, 2, 1), content = "Neu", approved = true)
        )

        val captor = argumentCaptor<Evaluation>()
        verify(evaluationRepository).save(captor.capture())
        assertThat(captor.firstValue.goal).isSameAs(goal)
        assertThat(captor.firstValue.createdBy).isSameAs(employee)
        assertThat(captor.firstValue.updatedBy).isSameAs(employee)
        assertThat(captor.firstValue.content).isEqualTo("Neu")
        assertThat(captor.firstValue.approved).isTrue()
        assertThat(result).isInstanceOf(EvaluationCreateResult.Success::class.java)
        val response = (result as EvaluationCreateResult.Success).response
        assertThat(response.goalId).isEqualTo(11L)
        assertThat(response.createdBy).isEqualTo("Muster Max")
    }

    @Test
    fun update_unknownEvaluation_returnsNotFound() {
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.empty())

        val result = service.update(EvaluationUpdateRequest(id = 100L))

        assertThat(result).isEqualTo(EvaluationUpdateResult.NotFound)
    }

    @Test
    fun update_withoutWriteAccess_returnsForbidden() {
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.of(evaluation(goal())))
        whenever(accessService.canWriteEntries(institutionId)).thenReturn(false)

        val result = service.update(EvaluationUpdateRequest(id = 100L, content = "Neu"))

        assertThat(result).isEqualTo(EvaluationUpdateResult.Forbidden)
        verify(evaluationRepository, never()).save(any<Evaluation>())
    }

    @Test
    fun update_archivedClient_returnsClientArchived() {
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.of(evaluation(goal(plan(archived = true)))))

        val result = service.update(EvaluationUpdateRequest(id = 100L, content = "Neu"))

        assertThat(result).isEqualTo(EvaluationUpdateResult.ClientArchived)
        verify(evaluationRepository, never()).save(any<Evaluation>())
    }

    @Test
    fun update_existingEvaluation_updatesFieldsAndUpdater() {
        val existing = evaluation(goal()).apply { createdBy = Employee(id = 8, firstname = "Anna", lastname = "Alt") }
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.of(existing))

        val result = service.update(
            EvaluationUpdateRequest(id = 100L, date = LocalDate.of(2026, 3, 3), content = "Neu", approved = true)
        )

        assertThat(result).isInstanceOf(EvaluationUpdateResult.Success::class.java)
        assertThat(existing.content).isEqualTo("Neu")
        assertThat(existing.date).isEqualTo(LocalDate.of(2026, 3, 3))
        assertThat(existing.approved).isTrue()
        assertThat(existing.updatedBy).isSameAs(employee)
        assertThat(existing.createdBy?.lastname).isEqualTo("Alt")
    }

    @Test
    fun delete_unknownEvaluation_returnsNotFound() {
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.empty())

        val result = service.delete(100L)

        assertThat(result).isEqualTo(EvaluationDeleteResult.NotFound)
        verify(evaluationRepository, never()).delete(any<Evaluation>())
    }

    @Test
    fun delete_withoutWriteAccess_returnsForbidden() {
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.of(evaluation(goal())))
        whenever(accessService.canWriteEntries(institutionId)).thenReturn(false)

        val result = service.delete(100L)

        assertThat(result).isEqualTo(EvaluationDeleteResult.Forbidden)
        verify(evaluationRepository, never()).delete(any<Evaluation>())
    }

    @Test
    fun delete_archivedClient_returnsClientArchived() {
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.of(evaluation(goal(plan(archived = true)))))

        val result = service.delete(100L)

        assertThat(result).isEqualTo(EvaluationDeleteResult.ClientArchived)
        verify(evaluationRepository, never()).delete(any<Evaluation>())
    }

    @Test
    fun delete_existingEvaluation_deletesAndReturnsDeleted() {
        val existing = evaluation(goal())
        whenever(evaluationRepository.findById(100L)).thenReturn(Optional.of(existing))

        val result = service.delete(100L)

        verify(evaluationRepository).delete(existing)
        assertThat((result as EvaluationDeleteResult.Success).response.id).isEqualTo(100L)
    }

    @Test
    fun getYearEvaluations_unknownAssistancePlan_returnsAssistancePlanNotFound() {
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(null)

        val result = service.getYearEvaluationsByAssistancePlanIdAndYear(1L, 2026)

        assertThat(result).isEqualTo(EvaluationYearResult.AssistancePlanNotFound)
    }

    @Test
    fun getYearEvaluations_withoutReadAccess_returnsForbidden() {
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(plan())
        whenever(accessService.canReadEntries(institutionId)).thenReturn(false)

        val result = service.getYearEvaluationsByAssistancePlanIdAndYear(1L, 2026)

        assertThat(result).isEqualTo(EvaluationYearResult.Forbidden)
    }

    @Test
    fun getYearEvaluations_mapsEvaluationsToGoalMonthsAndSortsGoals() {
        val plan = plan()
        val goalB = goal(plan, id = 12L, title = "B")
        val goalA = goal(plan, id = 11L, title = "A")
        val februaryOfA = evaluation(goalA, id = 100L, date = LocalDate.of(2026, 2, 10))
        val marchOfB = evaluation(goalB, id = 101L, date = LocalDate.of(2026, 3, 5))
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(plan)
        whenever(evaluationRepository.findAllByGoalIdIn(any())).thenReturn(listOf(marchOfB, februaryOfA))

        val result = service.getYearEvaluationsByAssistancePlanIdAndYear(1L, 2026)

        val response = (result as EvaluationYearResult.Success).response
        assertThat(response.year).isEqualTo(2026)
        assertThat(response.values.map { it.goalId }).containsExactly(11L, 12L)
        val monthsOfA = response.values[0].months
        assertThat(monthsOfA).hasSize(12)
        assertThat(monthsOfA[1].evaluation?.id).isEqualTo(100L)
        assertThat(monthsOfA[2].evaluation).isNull()
        assertThat(response.values[1].months[2].evaluation?.id).isEqualTo(101L)
        // Hilfeplan läuft 01.01. bis 30.06. -> Juli ist nicht mehr aktiv
        assertThat(monthsOfA[5].assistancePlanActive).isTrue()
        assertThat(monthsOfA[6].assistancePlanActive).isFalse()
    }
}
