package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanAnalysisService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/analysis/assistance_plans")
class AssistancePlanAnalysisController(
        private val assistancePlanAnalysisService: AssistancePlanAnalysisService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanAnalysisController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @GetMapping("/institution/sponsor/hour_type/{year}/{month}/{institutionId}/{sponsorId}/{hourTypeId}")
    fun getByYearAndMonthAndInstitutionIdAndHourTypeId(@PathVariable year: Int,
                                                       @PathVariable month: Int,
                                                       @PathVariable institutionId: Long,
                                                       @PathVariable sponsorId: Long,
                                                       @PathVariable hourTypeId: Long,
                                                       ): ResponseEntity<Any> {
        // performance
        val startMs = System.currentTimeMillis()

        val analysis = assistancePlanAnalysisService.getAnalysisByInstitutionAndSponsorAndHourTypeInMonth(
                year, month, institutionId, sponsorId, hourTypeId)

        if (logPerformance) {
            logger.info(String.format("%s getByYearAndMonthAndInstitutionIdAndHourTypeId took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(analysis)
    }

    @GetMapping("/institution/hour_type/{year}/{month}/{institutionId}/{hourTypeId}")
    fun getByYearAndMonthAndInstitutionIdAndHourTypeId(@PathVariable year: Int,
                                                       @PathVariable month: Int,
                                                       @PathVariable institutionId: Long,
                                                       @PathVariable hourTypeId: Long): ResponseEntity<Any> {
        // performance
        val startMs = System.currentTimeMillis()

        val analysis = assistancePlanAnalysisService.getAnalysisByInstitutionAndHourTypeInMonth(
                year, month, institutionId, hourTypeId)

        if (logPerformance) {
            logger.info(String.format("%s getByYearAndMonthAndInstitutionIdAndHourTypeId took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(analysis)
    }

    @GetMapping("/institution/{year}/{month}/{institutionId}")
    fun getByYearAndMonthAndInstitutionId(@PathVariable year: Int,
                                          @PathVariable month: Int,
                                          @PathVariable institutionId: Long): ResponseEntity<Any> {
        // performance
        val startMs = System.currentTimeMillis()

        val analysis = assistancePlanAnalysisService.getAnalysisByInstitutionInMonth(year, month, institutionId)

        if (logPerformance) {
            logger.info(String.format("%s getByYearAndMonthAndInstitutionId took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(analysis)
    }
}