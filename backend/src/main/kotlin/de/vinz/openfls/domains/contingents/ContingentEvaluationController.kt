package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.services.ContingentEvaluationService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contingents/evaluations")
class ContingentEvaluationController(
        private val contingentEvaluationService: ContingentEvaluationService
) {
    private val logger: Logger = LoggerFactory.getLogger(ContingentEvaluationController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @GetMapping("institution/{institutionId}/{year}")
    fun getByInstitution(@PathVariable institutionId: Long, @PathVariable year: Int): Any {
        // performance
        val startMs = System.currentTimeMillis()

        val contingentEvaluation =
                contingentEvaluationService.getContingentEvaluationByYearAndInstitution(year, institutionId)

        if (logPerformance) {
            logger.info(String.format("%s getByInstitution took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return ResponseEntity.ok(contingentEvaluation)
    }
}