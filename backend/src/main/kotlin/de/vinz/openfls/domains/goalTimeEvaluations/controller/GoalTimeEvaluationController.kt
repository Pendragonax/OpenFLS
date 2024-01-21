package de.vinz.openfls.domains.goalTimeEvaluations.controller

import de.vinz.openfls.domains.goalTimeEvaluations.services.GoalTimeEvaluationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    @GetMapping("{assistancePlanId}/{hourTypeId}/{year}")
    fun getByAssistancePlanIdAndHourTypeIdAndYear(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                                  @PathVariable assistancePlanId: Long,
                                                  @PathVariable hourTypeId: Long,
                                                  @PathVariable year: Int): Any {
        return try {
            ResponseEntity.ok(
                    goalTimeEvaluationService
                            .getByAssistancePlanIdAndHourTypeIdAndYear(assistancePlanId, hourTypeId, year))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }
}