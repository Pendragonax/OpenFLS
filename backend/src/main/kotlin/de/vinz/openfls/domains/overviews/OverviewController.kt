package de.vinz.openfls.domains.overviews

import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.CsvService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDate

@Controller
@RequestMapping("/overviews")
class OverviewController(
        private val overviewService: OverviewService
) {

    private val logger: Logger = LoggerFactory.getLogger(OverviewController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverview(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getExecutedHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        if (logPerformance) {
            logger.info(String.format("%s getExecutedHoursOverview took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/csv/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverviewAsCsv(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getExecutedHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-executed-$year-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        if (logPerformance) {
            logger.info(String.format("%s getExecutedHoursOverviewAsCsv took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverview(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getExecutedHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        if (logPerformance) {
            logger.info(String.format("%s getExecutedHoursOverview took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/csv/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverviewAsCsv(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getExecutedHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-executed-$year-$month-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        if (logPerformance) {
            logger.info(String.format("%s getExecutedHoursOverviewAsCsv took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHours(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getApprovedHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        if (logPerformance) {
            logger.info(String.format("%s getApprovedHours took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/csv/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHoursAsCsv(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getApprovedHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-approved-$year-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        if (logPerformance) {
            logger.info(String.format("%s getApprovedHoursAsCsv took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHours(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getApprovedHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        if (logPerformance) {
            logger.info(String.format("%s getApprovedHours took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/csv/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHoursAsCsv(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getApprovedHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-approved-$year-$month-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        if (logPerformance) {
            logger.info(String.format("%s getApprovedHoursAsCsv took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHours(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getDifferenceHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        if (logPerformance) {
            logger.info(String.format("%s getDifferenceHours took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/csv/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHoursAsCsv(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getDifferenceHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-difference-$year-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        if (logPerformance) {
            logger.info(String.format("%s getDifferenceHoursAsCsv took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHours(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getDifferenceHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        if (logPerformance) {
            logger.info(String.format("%s getDifferenceHours took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/csv/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHoursAsCsv(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val result = overviewService.getDifferenceHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-difference-$year-$month-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        if (logPerformance) {
            logger.info(String.format("%s getDifferenceHoursAsCsv took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    companion object {
        const val VALUE_TYPE_EXECUTED_HOURS = "EXECUTED_HOURS"
        const val VALUE_TYPE_APPROVED_HOURS = "APPROVED_HOURS"
        const val VALUE_TYPE_DIFFERENCE_HOURS = "DIFFERENCE_HOURS"
    }
}