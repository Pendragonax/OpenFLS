package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.services.projections.ServiceSoloProjection
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AssistancePlanEvaluationLeftServiceTest {

    @Mock
    lateinit var assistancePlanService: AssistancePlanService

    @Mock
    lateinit var serviceService: ServiceService

    private lateinit var evaluationService: AssistancePlanEvaluationLeftService

    @BeforeEach
    fun setUp() {
        evaluationService = AssistancePlanEvaluationLeftService(assistancePlanService, serviceService)
    }

    @Test
    fun createAssistancePlanHourTypeAnalysis_unknownAssistancePlan_throwsIllegalArgument() {
        // Given
        val date = LocalDate.of(2024, 2, 1)
        whenever(assistancePlanService.getById(999)).thenReturn(null)

        // When / Then
        assertThatThrownBy { evaluationService.createAssistancePlanHourTypeAnalysis(date, 999) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun createAssistancePlanHourTypeAnalysis_noHoursAndGoals_returnsEmptyEvaluation() {
        // Given
        val date = LocalDate.of(2024, 2, 1)
        val plan = AssistancePlan(id = 1, start = LocalDate.of(2024, 1, 1), end = LocalDate.of(2024, 12, 31))
        whenever(assistancePlanService.getById(1)).thenReturn(plan)

        // When
        val result = evaluationService.createAssistancePlanHourTypeAnalysis(date, 1)

        // Then
        assertThat(result.hourTypeEvaluation).isEmpty()
    }

    @Test
    fun createAssistancePlanHourTypeAnalysis_noServices_returnsZeroLeftForAllHourTypes() {
        // Given
        val date = LocalDate.of(2024, 2, 1)
        val hourType = HourType(id = 10, title = "Einzel")
        val goal = goalWithHourType(hourType, weeklyHours = 0.0)
        val plan = AssistancePlan(
            id = 2,
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 12, 31),
            goals = mutableSetOf(goal)
        )
        whenever(assistancePlanService.getById(2)).thenReturn(plan)
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndStartAndEnd(any(), any(), any(), any()))
            .thenReturn(emptyList())
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(any(), any(), any()))
            .thenReturn(emptyList())
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(any(), any(), any(), any()))
            .thenReturn(emptyList())

        // When
        val result = evaluationService.createAssistancePlanHourTypeAnalysis(date, 2)

        // Then
        assertThat(result.hourTypeEvaluation).hasSize(1)
        val evaluation = result.hourTypeEvaluation.first()
        assertThat(evaluation.hourTypeName).isEqualTo("Einzel")
        assertThat(evaluation.leftThisWeek).isEqualTo(0.0)
        assertThat(evaluation.leftThisMonth).isEqualTo(0.0)
        assertThat(evaluation.leftThisYear).isEqualTo(0.0)
    }

    @Test
    fun createAssistancePlanHourTypeAnalysis_planEndedLastYear_returnsZeroLeft() {
        // Given
        val date = LocalDate.of(2024, 2, 1)
        val hourType = HourType(id = 11, title = "Gruppe")
        val goal = goalWithHourType(hourType, weeklyHours = 7.0)
        val plan = AssistancePlan(
            id = 3,
            start = LocalDate.of(2023, 1, 1),
            end = LocalDate.of(2023, 12, 31),
            goals = mutableSetOf(goal)
        )
        whenever(assistancePlanService.getById(3)).thenReturn(plan)
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndStartAndEnd(any(), any(), any(), any()))
            .thenReturn(emptyList())
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(any(), any(), any()))
            .thenReturn(emptyList())
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(any(), any(), any(), any()))
            .thenReturn(emptyList())

        // When
        val result = evaluationService.createAssistancePlanHourTypeAnalysis(date, 3)

        // Then
        val evaluation = result.hourTypeEvaluation.first()
        assertThat(evaluation.leftThisWeek).isEqualTo(0.0)
        assertThat(evaluation.leftThisMonth).isEqualTo(0.0)
        assertThat(evaluation.leftThisYear).isEqualTo(0.0)
    }

    @Test
    fun createAssistancePlanHourTypeAnalysis_planEndedPreviousMonth_returnsYearLeftOnly() {
        // Given
        val date = LocalDate.of(2024, 3, 10)
        val hourType = HourType(id = 12, title = "Einzel")
        val goal = goalWithHourType(hourType, weeklyHours = 7.0)
        val plan = AssistancePlan(
            id = 4,
            start = LocalDate.of(2024, 1, 1),
            end = LocalDate.of(2024, 2, 28),
            goals = mutableSetOf(goal)
        )
        whenever(assistancePlanService.getById(4)).thenReturn(plan)
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndStartAndEnd(any(), any(), any(), any()))
            .thenReturn(emptyList())
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(any(), any(), any()))
            .thenReturn(emptyList())
        whenever(serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(any(), any(), any(), any()))
            .thenReturn(emptyList())

        val daysInYearRange = DateService.countDaysOfYearBetweenStartAndEnd(2024, plan.start, plan.end)
        val expectedLeftYear = TimeDoubleService.convertDoubleToTimeDouble(daysInYearRange.toDouble())

        // When
        val result = evaluationService.createAssistancePlanHourTypeAnalysis(date, 4)

        // Then
        val evaluation = result.hourTypeEvaluation.first()
        assertThat(evaluation.leftThisWeek).isEqualTo(0.0)
        assertThat(evaluation.leftThisMonth).isEqualTo(0.0)
        assertThat(evaluation.leftThisYear).isEqualTo(expectedLeftYear)
    }

    private fun goalWithHourType(hourType: HourType, weeklyHours: Double): Goal {
        val goal = Goal(title = "Goal")
        val goalHour = GoalHour(weeklyHours = weeklyHours, hourType = hourType, goal = goal)
        goal.hours.add(goalHour)
        return goal
    }

    @Suppress("unused")
    private fun serviceProjection(minutes: Int): ServiceSoloProjection {
        return object : ServiceSoloProjection {
            override val id: Long = 1
            override val start: LocalDateTime = LocalDateTime.of(2024, 2, 1, 8, 0)
            override val end: LocalDateTime = LocalDateTime.of(2024, 2, 1, 9, 0)
            override val minutes: Int = minutes
            override val title: String = ""
            override val content: String = ""
            override val unfinished: Boolean = false
            override val groupService: Boolean = false
        }
    }
}
