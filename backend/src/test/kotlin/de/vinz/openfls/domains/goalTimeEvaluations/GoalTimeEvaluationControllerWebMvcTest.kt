package de.vinz.openfls.domains.goalTimeEvaluations

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.goalTimeEvaluations.dto.GoalTimeEvaluationResult
import de.vinz.openfls.domains.goalTimeEvaluations.dto.GoalsTimeEvaluationResponse
import de.vinz.openfls.domains.goalTimeEvaluations.service.GoalTimeEvaluationService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(GoalTimeEvaluationController::class)
@AutoConfigureMockMvc(addFilters = false)
class GoalTimeEvaluationControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var goalTimeEvaluationService: GoalTimeEvaluationService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    private fun emptyMonthlyHours(): List<Double> = List(12) { 0.0 }

    @Test
    fun getByAssistancePlanIdAndHourTypeIdAndYear_success_returnsOk() {
        // Given
        val response = GoalsTimeEvaluationResponse(
            assistancePlanId = 1L,
            hourMode = AssistancePlanHourMode.EXACT,
            executedHours = emptyMonthlyHours(),
            summedExecutedHours = emptyMonthlyHours(),
            approvedHours = emptyMonthlyHours(),
            summedApprovedHours = emptyMonthlyHours(),
            approvedHoursLeft = emptyMonthlyHours(),
            summedApprovedHoursLeft = emptyMonthlyHours(),
            goalTimeEvaluations = emptyList()
        )
        given(goalTimeEvaluationService.getByAssistancePlanIdAndHourTypeIdAndYear(1L, 2L, 2026))
            .willReturn(GoalTimeEvaluationResult.Success(response))

        // When
        val result = mockMvc.get("/goal_evaluation/1/2/2026").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"assistancePlanId\":1")
    }

    @Test
    fun getByAssistancePlanIdAndHourTypeIdAndYear_unknownAssistancePlan_returnsNotFound() {
        // Given
        given(goalTimeEvaluationService.getByAssistancePlanIdAndHourTypeIdAndYear(99L, 2L, 2026))
            .willReturn(GoalTimeEvaluationResult.AssistancePlanNotFound)

        // When
        val result = mockMvc.get("/goal_evaluation/99/2/2026").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(404)
    }

    @Test
    fun getByAssistancePlanIdAndHourTypeIdAndYear_noGoalForHourType_returnsBadRequest() {
        // Given
        given(goalTimeEvaluationService.getByAssistancePlanIdAndHourTypeIdAndYear(1L, 999L, 2026))
            .willReturn(GoalTimeEvaluationResult.NoGoalFoundForHourType)

        // When
        val result = mockMvc.get("/goal_evaluation/1/999/2026").andReturn()

        // Then
        assertThat(result.response.status).isEqualTo(400)
    }
}
