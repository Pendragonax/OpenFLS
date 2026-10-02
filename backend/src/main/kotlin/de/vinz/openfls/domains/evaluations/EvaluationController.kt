package de.vinz.openfls.domains.evaluations

import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationCreateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationDeleteResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateRequest
import de.vinz.openfls.domains.evaluations.dto.EvaluationUpdateResult
import de.vinz.openfls.domains.evaluations.dto.EvaluationYearResult
import de.vinz.openfls.domains.evaluations.service.EvaluationService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/evaluations")
class EvaluationController(
        private val evaluationService: EvaluationService,
        private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(EvaluationController::class.java)

    @PostMapping
    fun create(@RequestBody request: EvaluationCreateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = evaluationService.create(request)) {
                is EvaluationCreateResult.Success -> ResponseEntity.ok(result.response)
                EvaluationCreateResult.GoalNotFound -> notFound("goal not found")
                EvaluationCreateResult.Forbidden -> forbidden("no permission to create evaluations for this goal")
                EvaluationCreateResult.ClientArchived -> clientArchived()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping
    fun update(@RequestBody request: EvaluationUpdateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = evaluationService.update(request)) {
                is EvaluationUpdateResult.Success -> ResponseEntity.ok(result.response)
                EvaluationUpdateResult.NotFound -> notFound("evaluation not found")
                EvaluationUpdateResult.Forbidden -> forbidden("no permission to update this evaluation")
                EvaluationUpdateResult.ClientArchived -> clientArchived()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = evaluationService.delete(id)) {
                is EvaluationDeleteResult.Success -> ResponseEntity.ok(result.response)
                EvaluationDeleteResult.NotFound -> notFound("evaluation not found")
                EvaluationDeleteResult.Forbidden -> forbidden("no permission to delete this evaluation")
                EvaluationDeleteResult.ClientArchived -> clientArchived()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping("assistance_plan/{assistancePlanId}/{year}")
    fun getYearEvaluationsByAssistancePlanIdAndYear(@PathVariable assistancePlanId: Long,
                                                    @PathVariable year: Int): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = evaluationService.getYearEvaluationsByAssistancePlanIdAndYear(assistancePlanId, year)) {
                is EvaluationYearResult.Success -> ResponseEntity.ok(result.response)
                EvaluationYearResult.AssistancePlanNotFound -> notFound("assistance plan not found")
                EvaluationYearResult.Forbidden -> forbidden("no permission to read the evaluations of this assistance plan")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getYearEvaluationsByAssistancePlanIdAndYear", startMs, logger)
        }
    }

    private fun notFound(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(message)

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)

    private fun clientArchived(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body("client is archived")
}
