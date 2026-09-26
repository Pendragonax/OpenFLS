package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.domains.hourTypes.dtos.HourTypeCreateRequest
import de.vinz.openfls.domains.hourTypes.dtos.HourTypeUpdateRequest
import de.vinz.openfls.domains.hourTypes.exceptions.InvalidHourTypeRequestException
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
            throw InvalidHourTypeRequestException("path id and request id are not the same")
        if (!hourTypeService.existsById(id))
            throw InvalidHourTypeRequestException("Type of hour with id $id does not exists.")

        return try {
            ResponseEntity.ok(hourTypeService.update(request))
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

        if (!hourTypeService.existsById(id))
            throw InvalidHourTypeRequestException("Type of hour with id $id does not exists.")

        return try {
            val dto = hourTypeService.getById(id)
            hourTypeService.delete(id)
            ResponseEntity.ok(dto)
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
            ResponseEntity.ok(hourTypeService.getById(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }
}