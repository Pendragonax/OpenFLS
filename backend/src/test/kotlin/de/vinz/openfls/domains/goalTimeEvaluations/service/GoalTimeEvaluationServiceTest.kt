package de.vinz.openfls.domains.goalTimeEvaluations.service

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.goalTimeEvaluations.dto.GoalTimeEvaluationResult
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.services.service.ServiceService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class GoalTimeEvaluationServiceTest {

    private val serviceService: ServiceService = mock()
    private val assistancePlanService: AssistancePlanService = mock()
    private lateinit var service: GoalTimeEvaluationService

    private val corridorHourType = HourType(id = 7, title = "FL", price = 5.0)
    private val year = 2026

    @BeforeEach
    fun setUp() {
        service = GoalTimeEvaluationService(serviceService, assistancePlanService)
        whenever(serviceService.getAllEntitiesByAssistancePlanIdAndStartBetween(any(), any(), any()))
            .thenReturn(emptyList())
    }

    private fun corridorPlan(from: Int = 420, till: Int = 630): AssistancePlan {
        val corridor = HourCorridor(
            id = 3,
            title = "Korridor",
            weeklyMinutesFrom = from,
            weeklyMinutesTill = till,
            hourType = corridorHourType
        )
        val plan = AssistancePlan(
            id = 1L,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31),
            hourMode = AssistancePlanHourMode.CORRIDOR,
            hourCorridor = corridor
        )
        plan.goals.add(Goal(id = 11L, title = "Ziel A", description = "A", assistancePlan = plan))
        plan.goals.add(Goal(id = 12L, title = "Ziel B", description = "B", assistancePlan = plan))
        return plan
    }

    @Test
    fun corridorPlan_matchingHourType_producesThreeAssistancePlanRowsFromTillAverage() {
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(corridorPlan())

        val result = getSuccess(1L, corridorHourType.id, year)

        assertThat(result.hourMode).isEqualTo(AssistancePlanHourMode.CORRIDOR)
        assertThat(result.corridorAssistancePlanEvaluations.map { it.title })
            .containsExactly("Untergrenze", "Obergrenze", "Durchschnitt")

        val from = result.corridorAssistancePlanEvaluations[0]
        val till = result.corridorAssistancePlanEvaluations[1]
        val avg = result.corridorAssistancePlanEvaluations[2]

        // approved hours ordered from <= average <= till per month, and non-zero within the plan period
        for (i in 0 until 12) {
            assertThat(from.approvedHours[i]).isLessThanOrEqualTo(avg.approvedHours[i])
            assertThat(avg.approvedHours[i]).isLessThanOrEqualTo(till.approvedHours[i])
        }
        assertThat(from.approvedHours.sum()).isGreaterThan(0.0)
        assertThat(till.approvedHours.sum()).isGreaterThan(from.approvedHours.sum())

        // no services -> executed 0 in every row; identical across the three rows
        assertThat(from.executedHours).hasSize(12).containsOnly(0.0)
        assertThat(from.summedExecutedHours).hasSize(12).containsOnly(0.0)
        assertThat(till.executedHours).isEqualTo(from.executedHours)
        assertThat(avg.executedHours).isEqualTo(from.executedHours)

        // executed == 0 -> "approved left" equals "approved"
        assertThat(avg.approvedHoursLeft.sum()).isCloseTo(avg.approvedHours.sum(), within(0.01))
        assertThat(avg.summedApprovedHoursLeft.sum()).isCloseTo(avg.summedApprovedHours.sum(), within(0.01))
    }

    @Test
    fun corridorPlan_goalsCarryOnlyExecutedHoursEverythingElseZero() {
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(corridorPlan())

        val result = getSuccess(1L, corridorHourType.id, year)

        assertThat(result.goalTimeEvaluations.map { it.title }).containsExactly("Ziel A", "Ziel B")
        result.goalTimeEvaluations.forEach { goal ->
            assertThat(goal.executedHours).hasSize(12).containsOnly(0.0)
            assertThat(goal.summedExecutedHours).hasSize(12).containsOnly(0.0)
            assertThat(goal.approvedHours).hasSize(12).containsOnly(0.0)
            assertThat(goal.summedApprovedHours).hasSize(12).containsOnly(0.0)
            assertThat(goal.approvedHoursLeft).hasSize(12).containsOnly(0.0)
            assertThat(goal.summedApprovedHoursLeft).hasSize(12).containsOnly(0.0)
        }

        // top-level aggregate: executed populated (0 here), approved all 0
        assertThat(result.executedHours).hasSize(12).containsOnly(0.0)
        assertThat(result.approvedHours).hasSize(12).containsOnly(0.0)
        assertThat(result.summedApprovedHours).hasSize(12).containsOnly(0.0)
        assertThat(result.approvedHoursLeft).hasSize(12).containsOnly(0.0)
        assertThat(result.summedApprovedHoursLeft).hasSize(12).containsOnly(0.0)
    }

    @Test
    fun corridorPlan_summedApprovedHoursAreMonotonicallyIncreasing() {
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(corridorPlan())

        val result = getSuccess(1L, corridorHourType.id, year)
        val avgSummed = result.corridorAssistancePlanEvaluations[2].summedApprovedHours

        for (i in 1 until 12) {
            assertThat(avgSummed[i]).isGreaterThanOrEqualTo(avgSummed[i - 1])
        }
        assertThat(avgSummed[11]).isGreaterThan(avgSummed[0])
    }

    @Test
    fun corridorPlan_foreignHourType_returnsEmptyResultWithoutError() {
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(corridorPlan())

        val result = getSuccess(1L, 999L, year)

        assertThat(result.hourMode).isEqualTo(AssistancePlanHourMode.CORRIDOR)
        assertThat(result.corridorAssistancePlanEvaluations.map { it.title })
            .containsExactly("Untergrenze", "Obergrenze", "Durchschnitt")
        result.corridorAssistancePlanEvaluations.forEach { row ->
            assertThat(row.approvedHours).hasSize(12).containsOnly(0.0)
            assertThat(row.summedApprovedHours).hasSize(12).containsOnly(0.0)
            assertThat(row.executedHours).hasSize(12).containsOnly(0.0)
        }
        assertThat(result.goalTimeEvaluations).hasSize(2)
        result.goalTimeEvaluations.forEach { assertThat(it.executedHours).hasSize(12).containsOnly(0.0) }
    }

    @Test
    fun corridorPlan_yearOutsidePlanPeriod_returnsZeroedApprovedHours() {
        whenever(assistancePlanService.getEntityById(1L)).thenReturn(corridorPlan())

        val result = getSuccess(1L, corridorHourType.id, 2020)

        result.corridorAssistancePlanEvaluations.forEach { row ->
            assertThat(row.approvedHours).hasSize(12).containsOnly(0.0)
            assertThat(row.summedApprovedHours).hasSize(12).containsOnly(0.0)
        }
    }

    @Test
    fun exactPlan_withoutMatchingGoalOrPlanHours_returnsNoGoalFoundForHourType() {
        val plan = AssistancePlan(
            id = 2L,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31),
            hourMode = AssistancePlanHourMode.EXACT
        )
        whenever(assistancePlanService.getEntityById(2L)).thenReturn(plan)

        val result = service.getByAssistancePlanIdAndHourTypeIdAndYear(2L, 1L, year)

        assertThat(result).isEqualTo(GoalTimeEvaluationResult.NoGoalFoundForHourType)
    }

    @Test
    fun unknownAssistancePlan_returnsAssistancePlanNotFound() {
        whenever(assistancePlanService.getEntityById(99L)).thenReturn(null)

        val result = service.getByAssistancePlanIdAndHourTypeIdAndYear(99L, 1L, year)

        assertThat(result).isEqualTo(GoalTimeEvaluationResult.AssistancePlanNotFound)
    }

    private fun getSuccess(assistancePlanId: Long, hourTypeId: Long, year: Int) =
        (service.getByAssistancePlanIdAndHourTypeIdAndYear(assistancePlanId, hourTypeId, year)
                as GoalTimeEvaluationResult.Success).response
}
