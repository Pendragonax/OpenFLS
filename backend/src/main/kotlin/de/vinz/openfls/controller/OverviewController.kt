package de.vinz.openfls.controller

import de.vinz.openfls.services.CsvService
import de.vinz.openfls.services.OverviewService
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
    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverview(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getExecutedHoursOverview(
                token = token,
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/csv/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverviewAsCsv(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getExecutedHoursOverview(
                token = token,
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-executed-$year-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverview(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getExecutedHoursOverview(
                token = token,
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/csv/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverviewAsCsv(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getExecutedHoursOverview(
                token = token,
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-executed-$year-$month-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHours(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getApprovedHoursOverview(
                token = token,
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/csv/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHoursAsCsv(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getApprovedHoursOverview(
                token = token,
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-approved-$year-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHours(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getApprovedHoursOverview(
                token = token,
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/csv/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHoursAsCsv(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getApprovedHoursOverview(
                token = token,
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-approved-$year-$month-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHours(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getDifferenceHoursOverview(
                token = token,
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/csv/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHoursAsCsv(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getDifferenceHoursOverview(
                token = token,
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-difference-$year-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${filename}")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file)
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHours(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getDifferenceHoursOverview(
                token = token,
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/csv/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHoursAsCsv(
            @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getDifferenceHoursOverview(
                token = token,
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)
        val filename = "${LocalDate.now()}-difference-$year-$month-overview.csv"
        val file = InputStreamResource(CsvService.getCsvFileStream(result))

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