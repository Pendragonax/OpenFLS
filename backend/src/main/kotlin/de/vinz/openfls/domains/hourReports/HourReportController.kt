package de.vinz.openfls.domains.hourReports

import de.vinz.openfls.domains.hourReports.dto.HourReportResult
import de.vinz.openfls.domains.hourReports.service.HourReportService
import de.vinz.openfls.logging.StructuredLog
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hour_reports")
class HourReportController(
        private val hourReportService: HourReportService,
        private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(HourReportController::class.java)

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursReport(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponseEntity(hourReportService.getExecutedHoursReport(
                    year = year,
                    month = null,
                    hourTypeId = hourTypeId,
                    areaId = normalizeId(areaId),
                    sponsorId = normalizeId(sponsorId)))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "hourReport.executedHours.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getExecutedHoursReport", startMs, logger)
        }
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursReport(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponseEntity(hourReportService.getExecutedHoursReport(
                    year = year,
                    month = month,
                    hourTypeId = hourTypeId,
                    areaId = normalizeId(areaId),
                    sponsorId = normalizeId(sponsorId)))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "hourReport.executedHours.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getExecutedHoursReport", startMs, logger)
        }
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS_GROUP_OFFER")
    fun getExecutedHoursGroupServiceReport(
        @PathVariable year: Int,
        @PathVariable hourTypeId: Long,
        @PathVariable areaId: Long,
        @PathVariable sponsorId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponseEntity(hourReportService.getExecutedHoursGroupServiceReport(
                year = year,
                hourTypeId = hourTypeId,
                areaId = normalizeId(areaId),
                sponsorId = normalizeId(sponsorId)))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "hourReport.executedHoursGroupService.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getExecutedHoursGroupServiceReport", startMs, logger)
        }
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHoursReport(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponseEntity(hourReportService.getApprovedHoursReport(
                    year = year,
                    month = null,
                    hourTypeId = hourTypeId,
                    areaId = normalizeId(areaId),
                    sponsorId = normalizeId(sponsorId)))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "hourReport.approvedHours.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getApprovedHoursReport", startMs, logger)
        }
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHoursReport(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponseEntity(hourReportService.getApprovedHoursReport(
                    year = year,
                    month = month,
                    hourTypeId = hourTypeId,
                    areaId = normalizeId(areaId),
                    sponsorId = normalizeId(sponsorId)))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "hourReport.approvedHours.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getApprovedHoursReport", startMs, logger)
        }
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHoursReport(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponseEntity(hourReportService.getDifferenceHoursReport(
                    year = year,
                    month = null,
                    hourTypeId = hourTypeId,
                    areaId = normalizeId(areaId),
                    sponsorId = normalizeId(sponsorId)))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "hourReport.differenceHours.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getDifferenceHoursReport", startMs, logger)
        }
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHoursReport(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponseEntity(hourReportService.getDifferenceHoursReport(
                    year = year,
                    month = month,
                    hourTypeId = hourTypeId,
                    areaId = normalizeId(areaId),
                    sponsorId = normalizeId(sponsorId)))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "hourReport.differenceHours.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getDifferenceHoursReport", startMs, logger)
        }
    }

    private fun toResponseEntity(result: HourReportResult): ResponseEntity<*> = when (result) {
        is HourReportResult.Success -> ResponseEntity.ok(result.rows)
        HourReportResult.Forbidden -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("user not allowed")
        HourReportResult.InvalidTimeRange -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid year or month")
    }

    /** Das Frontend sendet `0` als Sentinel für "kein Filter". */
    private fun normalizeId(id: Long): Long? = if (id == 0L) null else id

    companion object {
        const val VALUE_TYPE_EXECUTED_HOURS = "EXECUTED_HOURS"
        const val VALUE_TYPE_EXECUTED_HOURS_GROUP_OFFER = "EXECUTED_HOURS_GROUP_OFFER"
        const val VALUE_TYPE_APPROVED_HOURS = "APPROVED_HOURS"
        const val VALUE_TYPE_DIFFERENCE_HOURS = "DIFFERENCE_HOURS"
    }
}
