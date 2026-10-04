package de.vinz.openfls.domains.hourCorridors

import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateRequest
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorDeleteResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateRequest
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateResult
import de.vinz.openfls.domains.hourCorridors.service.HourCorridorService
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
@RequestMapping("/hour_corridors")
class HourCorridorController(
    private val hourCorridorService: HourCorridorService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(HourCorridorController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: HourCorridorCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to add hour corridors")

        return try {
            when (val result = hourCorridorService.create(request)) {
                is HourCorridorCreateResult.Success -> ResponseEntity.ok(result.response)
                is HourCorridorCreateResult.InvalidRange -> ResponseEntity.badRequest().body(result.message)
                is HourCorridorCreateResult.HourTypeNotFound -> ResponseEntity.badRequest().body(result.message)
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: HourCorridorUpdateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to update hour corridors")
        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")

        return try {
            when (val result = hourCorridorService.update(request)) {
                is HourCorridorUpdateResult.Success -> ResponseEntity.ok(result.response)
                HourCorridorUpdateResult.NotFound -> hourCorridorNotFound()
                is HourCorridorUpdateResult.InvalidRange -> ResponseEntity.badRequest().body(result.message)
                is HourCorridorUpdateResult.HourTypeNotFound -> ResponseEntity.badRequest().body(result.message)
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
            return forbidden("no permission to delete hour corridors")

        return try {
            when (val result = hourCorridorService.delete(id)) {
                is HourCorridorDeleteResult.Success -> ResponseEntity.ok(result.response)
                HourCorridorDeleteResult.NotFound -> hourCorridorNotFound()
                is HourCorridorDeleteResult.Conflict -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("hour corridor is used by ${result.assistancePlanCount} assistance plans")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping
    fun getAll(): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(hourCorridorService.getAll())
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            val hourCorridor = hourCorridorService.getById(id) ?: return hourCorridorNotFound()
            ResponseEntity.ok(hourCorridor)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    @GetMapping("count/assistance_plan/{id}")
    fun countByAssistancePlan(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(hourCorridorService.countAssistancePlansByHourCorridorId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("countByAssistancePlan", startMs, logger)
        }
    }

    @GetMapping("{id}/history")
    fun getHistory(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(hourCorridorService.getAuditHistoryByHourCorridorId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getHistory", startMs, logger)
        }
    }

    @GetMapping("{id}/assistance-plans")
    fun getAssistancePlans(@PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(hourCorridorService.getAssistancePlansByHourCorridorId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        }
    }

    private fun hourCorridorNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("hour corridor not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)
}
