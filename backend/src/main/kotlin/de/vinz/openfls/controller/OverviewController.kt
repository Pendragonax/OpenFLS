package de.vinz.openfls.controller

import de.vinz.openfls.services.OverviewService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/overviews")
class OverviewController(
        private val overviewService: OverviewService
) {
    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverview(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getExecutedHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getExecutedHoursOverview(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getExecutedHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHours(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getApprovedHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_APPROVED_HOURS")
    fun getApprovedHours(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getApprovedHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("year/{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHours(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getDifferenceHoursOverview(
                year = year,
                month = null,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    @GetMapping("month/{year}/{month}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_DIFFERENCE_HOURS")
    fun getDifferenceHours(
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getDifferenceHoursOverview(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = if (areaId.toInt() == 0) null else areaId,
                sponsorId = if (sponsorId.toInt() == 0) null else sponsorId)

        return ResponseEntity.ok(result);
    }

    companion object {
        const val VALUE_TYPE_EXECUTED_HOURS = "EXECUTED_HOURS"
        const val VALUE_TYPE_APPROVED_HOURS = "APPROVED_HOURS"
        const val VALUE_TYPE_DIFFERENCE_HOURS = "DIFFERENCE_HOURS"
    }
}