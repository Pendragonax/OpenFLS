package de.vinz.openfls.domains.hourReports

import de.vinz.openfls.domains.hourReports.dto.HourReportMonthlySummaryResponse
import de.vinz.openfls.domains.hourReports.dto.HourReportMonthlySummaryResult
import de.vinz.openfls.domains.hourReports.service.HourReportMonthlySummaryService
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

@WebMvcTest(HourReportMonthlySummaryController::class)
@AutoConfigureMockMvc(addFilters = false)
class HourReportMonthlySummaryControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var hourReportMonthlySummaryService: HourReportMonthlySummaryService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    private val url = "/hour_reports/month_summary/2026/5/1/2/3"

    @Test
    fun getMonthlySummary_returnsSummary() {
        given(hourReportMonthlySummaryService.getMonthlySummary(2026, 5, 1, 2, 3)).willReturn(
            HourReportMonthlySummaryResult.Success(
                HourReportMonthlySummaryResponse(2026, 5, 10.0, 8.0, 80.0, 2.0, emptyList())
            )
        )

        val result = mockMvc.get(url).andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentAsString).contains("\"approvedHours\":10.0")
    }

    @Test
    fun getMonthlySummary_withoutPermission_returnsForbidden() {
        given(hourReportMonthlySummaryService.getMonthlySummary(2026, 5, 1, 2, 3))
            .willReturn(HourReportMonthlySummaryResult.Forbidden)

        assertThat(mockMvc.get(url).andReturn().response.status).isEqualTo(403)
    }

    @Test
    fun getMonthlySummary_invalidPeriod_returnsBadRequest() {
        given(hourReportMonthlySummaryService.getMonthlySummary(2026, 5, 1, 2, 3))
            .willReturn(HourReportMonthlySummaryResult.InvalidPeriod)

        assertThat(mockMvc.get(url).andReturn().response.status).isEqualTo(400)
    }
}
