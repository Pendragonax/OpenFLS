package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanExistingResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPreviewListResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPreviewResponse
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanPreviewService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@WebMvcTest(AssistancePlanPreviewController::class)
@AutoConfigureMockMvc(addFilters = false)
class AssistancePlanPreviewControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var assistancePlanPreviewService: AssistancePlanPreviewService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getPreviewsByClientId_returnsPreviews() {
        given(assistancePlanPreviewService.getPreviewsOfCurrentUserByClientId(3L))
            .willReturn(listOf(previewResponse(id = 1L, isFavorite = true)))

        val result = mockMvc.get("/assistance_plans/client/3/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":1")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":true")
    }

    @Test
    fun getPreviewsByClientId_unknownClient_returnsNotFound() {
        given(assistancePlanPreviewService.getPreviewsOfCurrentUserByClientId(3L)).willReturn(null)

        assertThat(mockMvc.get("/assistance_plans/client/3/preview").andReturn().response.status).isEqualTo(404)
    }

    @Test
    fun getPreviewsByInstitutionId_returnsPreviews() {
        given(assistancePlanPreviewService.getPreviewsByInstitutionId(9L)).willReturn(
            AssistancePlanPreviewListResult.Success(listOf(previewResponse(id = 2L, isFavorite = false)))
        )

        val result = mockMvc.get("/assistance_plans/institution/9/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":2")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":false")
    }

    @Test
    fun getPreviewsByInstitutionId_withoutReadRights_returnsForbidden() {
        given(assistancePlanPreviewService.getPreviewsByInstitutionId(9L))
            .willReturn(AssistancePlanPreviewListResult.Forbidden)

        assertThat(mockMvc.get("/assistance_plans/institution/9/preview").andReturn().response.status).isEqualTo(403)
    }

    @Test
    fun getPreviewsBySponsorId_returnsPreviews() {
        given(assistancePlanPreviewService.getPreviewsBySponsorId(5L))
            .willReturn(listOf(previewResponse(id = 3L, isFavorite = false)))

        val result = mockMvc.get("/assistance_plans/sponsor/5/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":3")
    }

    @Test
    fun getFavoritePreviews_returnsPreviews() {
        given(assistancePlanPreviewService.getFavoritePreviews())
            .willReturn(listOf(previewResponse(id = 4L, isFavorite = true)))

        val result = mockMvc.get("/assistance_plans/favorites/preview").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":4")
        assertThat(result.response.contentAsString).contains("\"isFavorite\":true")
    }

    @Test
    fun getFavoritePreviews_serviceThrows_returnsBadRequest() {
        doThrow(IllegalArgumentException("boom")).`when`(assistancePlanPreviewService).getFavoritePreviews()

        assertThat(mockMvc.get("/assistance_plans/favorites/preview").andReturn().response.status).isEqualTo(400)
    }

    @Test
    fun getExistingByClientId_returnsExistingPlans() {
        given(assistancePlanPreviewService.getExistingByClientId(3L)).willReturn(
            listOf(
                AssistancePlanExistingResponse(
                    id = 99L,
                    start = LocalDate.of(2026, 1, 1),
                    end = LocalDate.of(2026, 3, 31),
                    sponsorName = "LWV",
                    clientArchived = false
                )
            )
        )

        val result = mockMvc.get("/assistance_plans/client/3/existing").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"id\":99")
        assertThat(result.response.contentAsString).contains("\"sponsorName\":\"LWV\"")
    }

    @Test
    fun getExistingByClientId_unknownClient_returnsNotFound() {
        given(assistancePlanPreviewService.getExistingByClientId(3L)).willReturn(null)

        assertThat(mockMvc.get("/assistance_plans/client/3/existing").andReturn().response.status).isEqualTo(404)
    }

    private fun previewResponse(id: Long, isFavorite: Boolean): AssistancePlanPreviewResponse {
        return AssistancePlanPreviewResponse(
            id = id,
            clientId = 11,
            institutionId = 12,
            sponsorId = 13,
            clientFirstname = "Max",
            clientLastname = "Mustermann",
            institutionName = "Schule",
            sponsorName = "Kostentraeger",
            clientArchived = false,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31),
            isActive = true,
            isFavorite = isFavorite,
            hasIllegalHours = false,
            hourMode = AssistancePlanHourMode.EXACT,
            approvedHoursFrom = 7.0,
            approvedHoursTo = 7.0,
            approvedHoursPerWeek = 7.0,
            approvedHoursThisYearFrom = 366.0,
            approvedHoursThisYearTill = 366.0,
            approvedHoursThisYear = 366.0,
            executedHoursThisYear = 100.0,
            approvedHoursLeftThisYear = 266.0,
            approvedHoursThisAssistancePlanFrom = 366.0,
            approvedHoursThisAssistancePlanTill = 366.0,
            approvedHoursThisAssistancePlan = 366.0,
            executedHoursThisAssistancePlan = 100.0,
            approvedHoursLeftThisAssistancePlan = 266.0
        )
    }
}
