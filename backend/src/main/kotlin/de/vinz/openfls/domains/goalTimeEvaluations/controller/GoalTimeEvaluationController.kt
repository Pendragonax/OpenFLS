package de.vinz.openfls.domains.goalTimeEvaluations.controller

import de.vinz.openfls.domains.goalTimeEvaluations.services.GoalTimeEvaluationService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/goal_evaluation")
class GoalTimeEvaluationController(
        private val goalTimeEvaluationService: GoalTimeEvaluationService
) {
    private val logger: Logger = LoggerFactory.getLogger(GoalTimeEvaluationController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @GetMapping("{assistancePlanId}/{hourTypeId}/{year}")
    fun getByAssistancePlanIdAndHourTypeIdAndYear(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                                  @PathVariable assistancePlanId: Long,
                                                  @PathVariable hourTypeId: Long,
                                                  @PathVariable year: Int): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = goalTimeEvaluationService
                    .getByAssistancePlanIdAndHourTypeIdAndYear(assistancePlanId, hourTypeId, year)

            if (logPerformance) {
                logger.info(String.format("%s getByAssistancePlanIdAndHourTypeIdAndYear took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }
}