package de.vinz.openfls.controller

import de.vinz.openfls.exceptions.ValueTypeNotFoundException
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
    @GetMapping("{year}/{hourTypeId}/{areaId}/{sponsorId}/$VALUE_TYPE_EXECUTED_HOURS")
    fun getByYearAndHourTypeIdAndAreaIdAndSponsorIdAndValueTypeId(
            @PathVariable year: Int,
            @PathVariable hourTypeId: Long,
            @PathVariable areaId: Long,
            @PathVariable sponsorId: Long): Any {
        val result = overviewService.getExecutedHoursOverviewFromAssistancePlanByYear(
                    year,
                    hourTypeId,
                    areaId,
                    sponsorId);

        return ResponseEntity.ok(result);
    }

    companion object {
        const val VALUE_TYPE_EXECUTED_HOURS = "EXECUTED_HOURS";
    }
}