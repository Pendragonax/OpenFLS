package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanDeleteResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateResult
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/assistance_plans")
class AssistancePlanController(
    private val assistancePlanService: AssistancePlanService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanController::class.java)

    @PostMapping("")
    fun create(@Valid @RequestBody request: AssistancePlanCreateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = assistancePlanService.create(request)) {
                is AssistancePlanCreateResult.Success -> ResponseEntity.ok(result.response)
                is AssistancePlanCreateResult.InvalidHours -> badRequest(result.reason)
                AssistancePlanCreateResult.HourCorridorNotFound -> badRequest("hour corridor not found")
                AssistancePlanCreateResult.ClientNotFound -> badRequest("client not found")
                AssistancePlanCreateResult.ClientArchived -> clientArchived()
                AssistancePlanCreateResult.InstitutionNotFound -> badRequest("institution not found")
                AssistancePlanCreateResult.SponsorNotFound -> badRequest("sponsor not found")
                AssistancePlanCreateResult.HourTypeNotFound -> badRequest("hour type not found")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: AssistancePlanUpdateRequest): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.canModifyAssistancePlan(id))
            return forbidden("no permission to update this assistance plan")
        if (id != request.id)
            return badRequest("path id and request id are not the same")

        return try {
            when (val result = assistancePlanService.update(id, request)) {
                is AssistancePlanUpdateResult.Success -> ResponseEntity.ok(result.response)
                AssistancePlanUpdateResult.NotFound -> notFound()
                AssistancePlanUpdateResult.HourModeChanged ->
                    conflict("assistance plan hour mode cannot be changed")
                is AssistancePlanUpdateResult.InvalidHours -> badRequest(result.reason)
                AssistancePlanUpdateResult.HourCorridorNotFound -> badRequest("hour corridor not found")
                AssistancePlanUpdateResult.ClientNotFound -> badRequest("client not found")
                AssistancePlanUpdateResult.ClientArchived -> clientArchived()
                AssistancePlanUpdateResult.InstitutionNotFound -> badRequest("institution not found")
                AssistancePlanUpdateResult.SponsorNotFound -> badRequest("sponsor not found")
                AssistancePlanUpdateResult.HourTypeNotFound -> badRequest("hour type not found")
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

        if (!accessService.isAdmin())
            return forbidden("no permission to delete assistance plans")

        return try {
            when (val result = assistancePlanService.delete(id)) {
                is AssistancePlanDeleteResult.Success -> ResponseEntity.ok(result.response)
                AssistancePlanDeleteResult.NotFound -> notFound()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping("{id}/edit")
    fun getEditById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val assistancePlan = assistancePlanService.getEditById(
                id,
                includeArchived = accessService.isAdmin(),
                leadingInstitutionIds = accessService.getLeadingInstitutionIds()
            ) ?: return notFound()
            ResponseEntity.ok(assistancePlan)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getEditById", startMs, logger)
        }
    }

    @GetMapping("{id}/detail")
    fun getDetailById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val assistancePlan = assistancePlanService.getDetailById(id) ?: return notFound()
            ResponseEntity.ok(assistancePlan)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getDetailById", startMs, logger)
        }
    }

    private fun notFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("assistance plan not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)

    private fun badRequest(message: String): ResponseEntity<String> =
        ResponseEntity.badRequest().body(message)

    private fun conflict(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(message)

    private fun clientArchived(): ResponseEntity<String> = conflict("client is archived")
}
