package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanAnalysisService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/analysis/assistance_plans")
class AssistancePlanAnalysisController(
        private val assistancePlanAnalysisService: AssistancePlanAnalysisService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanAnalysisController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @GetMapping("/hour_type/{year}/{month}/{institutionId}/{hourTypeId}")
    fun getByYearAndMonthAndInstitutionIdAndHourTypeId(@PathVariable year: Int,
                                                       @PathVariable month: Int,
                                                       @PathVariable institutionId: Long,
                                                       @PathVariable hourTypeId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(assistancePlanAnalysisService
                .getAnalysisByInstitutionAndHourTypeInMonth(year, month, institutionId, hourTypeId))
    }

    @GetMapping("/all/{year}/{month}/{institutionId}")
    fun getByYearAndMonthAndInstitutionId(@PathVariable year: Int,
                                          @PathVariable month: Int,
                                          @PathVariable institutionId: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(assistancePlanAnalysisService
                .getAnalysisByInstitutionInMonth(year, month, institutionId))
    }
}