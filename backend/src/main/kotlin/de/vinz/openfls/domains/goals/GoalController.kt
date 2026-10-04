package de.vinz.openfls.domains.goals

import de.vinz.openfls.domains.goals.dto.GoalCreateRequest
import de.vinz.openfls.domains.goals.dto.GoalCreateResult
import de.vinz.openfls.domains.goals.dto.GoalDeleteResult
import de.vinz.openfls.domains.goals.dto.GoalUpdateRequest
import de.vinz.openfls.domains.goals.dto.GoalUpdateResult
import de.vinz.openfls.domains.goals.service.GoalService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/goals")
class GoalController(
    private val goalService: GoalService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(GoalController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: GoalCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!accessService.canModifyAssistancePlan(request.assistancePlanId))
            return forbidden("no permission to create goals for this assistance plan")

        return try {
            when (val result = goalService.create(request)) {
                is GoalCreateResult.Success -> ResponseEntity.ok(result.response)
                is GoalCreateResult.AssistancePlanNotFound -> ResponseEntity.badRequest().body(result.message)
                is GoalCreateResult.InstitutionNotFound -> ResponseEntity.badRequest().body(result.message)
                is GoalCreateResult.HourTypeNotFound -> ResponseEntity.badRequest().body(result.message)
                is GoalCreateResult.CorridorHoursNotAllowed -> ResponseEntity.badRequest().body(result.message)
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: GoalUpdateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!accessService.canModifyAssistancePlan(request.assistancePlanId))
            return forbidden("no permission to update this goal for this assistance plan")
        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")

        return try {
            when (val result = goalService.update(request)) {
                is GoalUpdateResult.Success -> ResponseEntity.ok(result.response)
                GoalUpdateResult.NotFound -> goalNotFound()
                is GoalUpdateResult.AssistancePlanNotFound -> ResponseEntity.badRequest().body(result.message)
                is GoalUpdateResult.InstitutionNotFound -> ResponseEntity.badRequest().body(result.message)
                is GoalUpdateResult.HourTypeNotFound -> ResponseEntity.badRequest().body(result.message)
                is GoalUpdateResult.HourNotInGoal -> ResponseEntity.badRequest().body(result.message)
                is GoalUpdateResult.CorridorHoursNotAllowed -> ResponseEntity.badRequest().body(result.message)
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to delete this goal")

        return try {
            when (val result = goalService.delete(id)) {
                is GoalDeleteResult.Success -> ResponseEntity.ok(result.response)
                GoalDeleteResult.NotFound -> goalNotFound()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping("assistance_plan/{id}")
    fun getByAssistancePlanId(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(goalService.getByAssistancePlanId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getByAssistancePlanId", startMs, logger)
        }
    }

    private fun goalNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("goal not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)
}
