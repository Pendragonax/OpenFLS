package de.vinz.openfls.domains.evaluations

import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationDeleteResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationResponse
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationYearResponse
import de.vinz.openfls.domains.evaluations.dto.EvaluationYearResult
import de.vinz.openfls.domains.evaluations.service.EvaluationService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(EvaluationController::class)
@AutoConfigureMockMvc(addFilters = false)
class EvaluationControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var evaluationService: EvaluationService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    private val createRequest = EvaluationCreateRequest(
        goalId = 11L, date = LocalDate.of(2026, 2, 1), content = "Inhalt", approved = false
    )
    // wie vom Frontend gesendet: ein gemeinsames Request-Modell mit id (Create) bzw. goalId (Update)
    private val createJson = """{"id":0,"goalId":11,"date":"2026-02-01","content":"Inhalt","approved":false}"""
    private val updateRequest = EvaluationUpdateRequest(
        id = 100L, date = LocalDate.of(2026, 2, 1), content = "Inhalt", approved = true
    )
    private val updateJson = """{"id":100,"goalId":11,"date":"2026-02-01","content":"Inhalt","approved":true}"""

    private fun response() = EvaluationResponse(
        id = 100L, goalId = 11L, date = LocalDate.of(2026, 2, 1), content = "Inhalt", approved = false,
        createdBy = "Muster Max", createdAt = LocalDateTime.of(2026, 2, 1, 8, 0),
        updatedBy = "Muster Max", updatedAt = LocalDateTime.of(2026, 2, 1, 8, 0)
    )

    private fun post(): Int = mockMvc.post("/evaluations") {
        contentType = MediaType.APPLICATION_JSON
        content = createJson
    }.andReturn().response.status

    private fun put(): Int = mockMvc.put("/evaluations") {
        contentType = MediaType.APPLICATION_JSON
        content = updateJson
    }.andReturn().response.status

    @Test
    fun create_success_returnsOk() {
        given(evaluationService.create(createRequest)).willReturn(EvaluationCreateResult.Success(response()))

        val result = mockMvc.post("/evaluations") {
            contentType = MediaType.APPLICATION_JSON
            content = createJson
        }.andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"goalId\":11")
    }

    @Test
    fun create_unknownGoal_returnsNotFound() {
        given(evaluationService.create(createRequest)).willReturn(EvaluationCreateResult.GoalNotFound)

        assertThat(post()).isEqualTo(404)
    }

    @Test
    fun create_forbidden_returns403() {
        given(evaluationService.create(createRequest)).willReturn(EvaluationCreateResult.Forbidden)

        assertThat(post()).isEqualTo(403)
    }

    @Test
    fun create_archivedClient_returns409() {
        given(evaluationService.create(createRequest)).willReturn(EvaluationCreateResult.ClientArchived)

        assertThat(post()).isEqualTo(409)
    }

    @Test
    fun update_success_returnsOk() {
        given(evaluationService.update(updateRequest)).willReturn(EvaluationUpdateResult.Success(response()))

        assertThat(put()).isEqualTo(200)
    }

    @Test
    fun update_unknownEvaluation_returnsNotFound() {
        given(evaluationService.update(updateRequest)).willReturn(EvaluationUpdateResult.NotFound)

        assertThat(put()).isEqualTo(404)
    }

    @Test
    fun update_forbidden_returns403() {
        given(evaluationService.update(updateRequest)).willReturn(EvaluationUpdateResult.Forbidden)

        assertThat(put()).isEqualTo(403)
    }

    @Test
    fun update_archivedClient_returns409() {
        given(evaluationService.update(updateRequest)).willReturn(EvaluationUpdateResult.ClientArchived)

        assertThat(put()).isEqualTo(409)
    }

    @Test
    fun delete_success_returnsOk() {
        given(evaluationService.delete(100L)).willReturn(EvaluationDeleteResult.Success(response()))

        val result = mockMvc.delete("/evaluations/100").andReturn()

        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun delete_unknownEvaluation_returnsNotFound() {
        given(evaluationService.delete(100L)).willReturn(EvaluationDeleteResult.NotFound)

        assertThat(mockMvc.delete("/evaluations/100").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun delete_forbidden_returns403() {
        given(evaluationService.delete(100L)).willReturn(EvaluationDeleteResult.Forbidden)

        assertThat(mockMvc.delete("/evaluations/100").andReturn().response.status).isEqualTo(403)
    }

    @Test
    fun delete_archivedClient_returns409() {
        given(evaluationService.delete(100L)).willReturn(EvaluationDeleteResult.ClientArchived)

        assertThat(mockMvc.delete("/evaluations/100").andReturn().response.status).isEqualTo(409)
    }

    @Test
    fun getYearEvaluations_success_returnsOk() {
        given(evaluationService.getYearEvaluationsByAssistancePlanIdAndYear(1L, 2026))
            .willReturn(EvaluationYearResult.Success(EvaluationYearResponse(2026, emptyList())))

        val result = mockMvc.get("/evaluations/assistance_plan/1/2026").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"year\":2026")
    }

    @Test
    fun getYearEvaluations_unknownAssistancePlan_returnsNotFound() {
        given(evaluationService.getYearEvaluationsByAssistancePlanIdAndYear(1L, 2026))
            .willReturn(EvaluationYearResult.AssistancePlanNotFound)

        assertThat(mockMvc.get("/evaluations/assistance_plan/1/2026").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getYearEvaluations_forbidden_returns403() {
        given(evaluationService.getYearEvaluationsByAssistancePlanIdAndYear(1L, 2026))
            .willReturn(EvaluationYearResult.Forbidden)

        assertThat(mockMvc.get("/evaluations/assistance_plan/1/2026").andReturn().response.status).isEqualTo(403)
    }
}
