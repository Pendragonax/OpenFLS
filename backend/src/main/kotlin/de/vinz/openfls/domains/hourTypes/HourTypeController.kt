package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.domains.hourTypes.exceptions.InvalidHourTypeDtoException
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
    fun create(@Valid @RequestBody value: HourTypeDto): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (value.id > 0)
            throw InvalidHourTypeDtoException("id is not 0")

        return try {
            ResponseEntity.ok(hourTypeService.create(value))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: HourTypeDto): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != valueDto.id)
            throw InvalidHourTypeDtoException("path id and dto id are not the same")
        if (!hourTypeService.existsById(id))
            throw InvalidHourTypeDtoException("Type of hour with id $id does not exists.")

        return try {
            ResponseEntity.ok(hourTypeService.update(valueDto))
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
            throw InvalidHourTypeDtoException("Type of hour with id $id does not exists.")

        return try {
            val dto = hourTypeService.getDtoById(id)
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
            ResponseEntity.ok(hourTypeService.getDtoById(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }
}