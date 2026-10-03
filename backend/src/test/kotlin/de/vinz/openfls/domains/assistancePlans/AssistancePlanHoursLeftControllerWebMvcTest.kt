package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dto.ApprovedHoursLeftHourTypeResponse
import de.vinz.openfls.domains.assistancePlans.dto.ApprovedHoursLeftResponse
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanHoursLeftService
import de.vinz.openfls.services.PerformanceLoggingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(AssistancePlanHoursLeftController::class)
@AutoConfigureMockMvc(addFilters = false)
class AssistancePlanHoursLeftControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var assistancePlanHoursLeftService: AssistancePlanHoursLeftService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    @Test
    fun getHoursLeftById_existingAssistancePlan_returnsHoursLeft() {
        given(assistancePlanHoursLeftService.getHoursLeftByAssistancePlanId(any(), org.mockito.kotlin.eq(7L)))
            .willReturn(
                ApprovedHoursLeftResponse(
                    assistancePlanId = 7L,
                    hourMode = AssistancePlanHourMode.EXACT,
                    approvedHoursFrom = 7.0,
                    approvedHoursTo = 7.0,
                    hourTypeEvaluation = listOf(ApprovedHoursLeftHourTypeResponse("Fachleistung", 1.0, 2.0, 3.0, 4.0))
                )
            )

        val result = mockMvc.get("/assistance_plans/7/hours_left").andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"assistancePlanId\":7")
        assertThat(result.response.contentAsString).contains("\"hourTypeName\":\"Fachleistung\"")
    }

    @Test
    fun getHoursLeftById_unknownAssistancePlan_returnsNotFound() {
        given(assistancePlanHoursLeftService.getHoursLeftByAssistancePlanId(any(), org.mockito.kotlin.eq(7L)))
            .willReturn(null)

        assertThat(mockMvc.get("/assistance_plans/7/hours_left").andReturn().response.status).isEqualTo(404)
    }
}
