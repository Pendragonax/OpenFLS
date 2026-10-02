package de.vinz.openfls.domains.hourReports

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEditDto
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.domains.hourReports.dto.HourReportRowResponse
import de.vinz.openfls.domains.hourReports.dto.HourReportResult
import de.vinz.openfls.domains.hourReports.service.HourReportService
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

@WebMvcTest(HourReportController::class)
@AutoConfigureMockMvc(addFilters = false)
class HourReportControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var hourReportService: HourReportService

    @MockitoBean
    lateinit var performanceLoggingService: PerformanceLoggingService

    private fun rowResponse(): HourReportRowResponse =
        HourReportRowResponse(AssistancePlanEditDto().apply { id = 1 }, ClientSimpleDto(), DoubleArray(13))

    @Test
    fun getExecutedHoursReport_success_returnsOk() {
        given(hourReportService.getExecutedHoursReport(2024, null, 1L, null, null))
            .willReturn(HourReportResult.Success(listOf(rowResponse())))

        val result = mockMvc.get("/hour_reports/year/2024/1/0/0/EXECUTED_HOURS").andReturn()

        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun getExecutedHoursReport_monthly_normalizesZeroIdsToNull() {
        given(hourReportService.getExecutedHoursReport(2024, 3, 1L, null, null))
            .willReturn(HourReportResult.Success(emptyList()))

        val result = mockMvc.get("/hour_reports/month/2024/3/1/0/0/EXECUTED_HOURS").andReturn()

        assertThat(result.response.status).isEqualTo(200)
    }

    @Test
    fun getApprovedHours_forbidden_returns403() {
        given(hourReportService.getApprovedHoursReport(2024, null, 1L, 9L, null))
            .willReturn(HourReportResult.Forbidden)

        val result = mockMvc.get("/hour_reports/year/2024/1/9/0/APPROVED_HOURS").andReturn()

        assertThat(result.response.status).isEqualTo(403)
    }

    @Test
    fun getDifferenceHours_invalidTimeRange_returns400() {
        given(hourReportService.getDifferenceHoursReport(2024, 13, 1L, null, null))
            .willReturn(HourReportResult.InvalidTimeRange)

        val result = mockMvc.get("/hour_reports/month/2024/13/1/0/0/DIFFERENCE_HOURS").andReturn()

        assertThat(result.response.status).isEqualTo(400)
    }

    @Test
    fun getExecutedHoursGroupServiceReport_success_returnsOk() {
        given(hourReportService.getExecutedHoursGroupServiceReport(2024, 1L, null, null))
            .willReturn(HourReportResult.Success(listOf(rowResponse())))

        val result = mockMvc.get("/hour_reports/year/2024/1/0/0/EXECUTED_HOURS_GROUP_OFFER").andReturn()

        assertThat(result.response.status).isEqualTo(200)
    }
}
