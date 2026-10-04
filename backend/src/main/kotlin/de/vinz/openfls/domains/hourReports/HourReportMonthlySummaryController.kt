package de.vinz.openfls.domains.hourReports

import de.vinz.openfls.domains.hourReports.dto.HourReportMonthlySummaryResult
import de.vinz.openfls.domains.hourReports.service.HourReportMonthlySummaryService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/hour_reports")
class HourReportMonthlySummaryController(
    private val hourReportMonthlySummaryService: HourReportMonthlySummaryService,
    private val performanceLoggingService: PerformanceLoggingService
) {
    private val logger: Logger = LoggerFactory.getLogger(HourReportMonthlySummaryController::class.java)

    @GetMapping("month_summary/{year}/{month}/{institutionId}/{sponsorId}/{hourTypeId}")
    fun getMonthlySummary(
        @PathVariable year: Int,
        @PathVariable month: Int,
        @PathVariable institutionId: Long,
        @PathVariable sponsorId: Long,
        @PathVariable hourTypeId: Long
    ): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = hourReportMonthlySummaryService.getMonthlySummary(
                year, month, institutionId, sponsorId, hourTypeId
            )) {
                is HourReportMonthlySummaryResult.Success -> ResponseEntity.ok(result.response)
                HourReportMonthlySummaryResult.Forbidden ->
                    ResponseEntity.status(HttpStatus.FORBIDDEN).body("no permission to analyse these assistance plans")
                HourReportMonthlySummaryResult.InvalidPeriod ->
                    ResponseEntity.badRequest().body("year or month is invalid")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getMonthlySummary", startMs, logger)
        }
    }
}
