package de.vinz.openfls.domains.goalTimeEvaluations

import de.vinz.openfls.domains.goalTimeEvaluations.dto.GoalTimeEvaluationResult
import de.vinz.openfls.domains.goalTimeEvaluations.service.GoalTimeEvaluationService
import de.vinz.openfls.logging.StructuredLog
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/goal_evaluation")
class GoalTimeEvaluationController(
        private val goalTimeEvaluationService: GoalTimeEvaluationService,
        private val performanceLoggingService: PerformanceLoggingService
) {
    private val logger: Logger = LoggerFactory.getLogger(GoalTimeEvaluationController::class.java)

    @GetMapping("{assistancePlanId}/{hourTypeId}/{year}")
    fun getByAssistancePlanIdAndHourTypeIdAndYear(@PathVariable assistancePlanId: Long,
                                                  @PathVariable hourTypeId: Long,
                                                  @PathVariable year: Int): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = goalTimeEvaluationService
                    .getByAssistancePlanIdAndHourTypeIdAndYear(assistancePlanId, hourTypeId, year)) {
                is GoalTimeEvaluationResult.Success -> ResponseEntity.ok(result.response)
                GoalTimeEvaluationResult.AssistancePlanNotFound -> assistancePlanNotFound(assistancePlanId)
                GoalTimeEvaluationResult.NoGoalFoundForHourType -> noGoalFoundForHourType(hourTypeId)
            }
        } catch (ex: Exception) {
            StructuredLog.error(logger, "goalTimeEvaluation.read.failed", ex)
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getByAssistancePlanIdAndHourTypeIdAndYear", startMs, logger)
        }
    }

    private fun assistancePlanNotFound(id: Long): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("assistance plan with id $id not found")

    private fun noGoalFoundForHourType(hourTypeId: Long): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("no matching goal found by the given hourType [id = $hourTypeId]")
}
