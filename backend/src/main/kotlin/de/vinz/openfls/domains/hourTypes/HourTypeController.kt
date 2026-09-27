package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.domains.hourTypes.dtos.HourTypeCreateRequest
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeDeleteResult
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeUpdateRequest
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeUpdateResult
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/hour_types")
class HourTypeController(private val hourTypeService: HourTypeService,
                         private val performanceLoggingService: PerformanceLoggingService) {

    private val logger: Logger = LoggerFactory.getLogger(HourTypeController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: HourTypeCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(hourTypeService.create(request))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody request: HourTypeUpdateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")

        return try {
            when (val result = hourTypeService.update(request)) {
                is HourTypeUpdateResult.Success -> ResponseEntity.ok(result.response)
                HourTypeUpdateResult.NotFound -> hourTypeNotFound(id)
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

        return try {
            when (val result = hourTypeService.delete(id)) {
                is HourTypeDeleteResult.Success -> ResponseEntity.ok(result.response)
                HourTypeDeleteResult.NotFound -> hourTypeNotFound(id)
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
            ResponseEntity.ok(hourTypeService.getAll())
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
            val hourType = hourTypeService.getById(id) ?: return hourTypeNotFound(id)
            ResponseEntity.ok(hourType)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    private fun hourTypeNotFound(id: Long): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("Type of hour with id $id does not exists.")
}
