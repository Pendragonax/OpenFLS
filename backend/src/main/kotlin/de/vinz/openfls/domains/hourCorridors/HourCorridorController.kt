package de.vinz.openfls.domains.hourCorridors

import de.vinz.openfls.domains.hourCorridors.dtos.CreateHourCorridorDto
import de.vinz.openfls.domains.hourCorridors.dtos.UpdateHourCorridorDto
import de.vinz.openfls.domains.hourCorridors.exceptions.InvalidHourCorridorDtoException
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody valueDto: CreateHourCorridorDto): Any {
        val startMs = System.currentTimeMillis()

        return try {
            if (!accessService.isAdmin()) throw IllegalAccessException("no permission to add hour corridors")
            ResponseEntity.ok(hourCorridorService.create(valueDto))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody valueDto: UpdateHourCorridorDto): Any {
        val startMs = System.currentTimeMillis()

        return try {
            if (!accessService.isAdmin()) throw IllegalAccessException("no permission to update hour corridors")
            if (id != valueDto.id) throw InvalidHourCorridorDtoException("path id and dto id are not the same")
            if (!hourCorridorService.existsById(id)) throw InvalidHourCorridorDtoException("hour corridor not found")
            ResponseEntity.ok(hourCorridorService.update(valueDto))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
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
            if (!accessService.isAdmin()) throw IllegalAccessException("no permission to delete hour corridors")
            if (!hourCorridorService.existsById(id)) throw InvalidHourCorridorDtoException("hour corridor not found")
            val dto = hourCorridorService.getDtoById(id)
            hourCorridorService.delete(id)
            ResponseEntity.ok(dto)
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping
    fun getAll(): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(hourCorridorService.getAll())
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(hourCorridorService.getDtoById(id))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    @GetMapping("count/assistance_plan/{id}")
    fun countByAssistancePlan(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(hourCorridorService.countByAssistancePlan(id))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("countByAssistancePlan", startMs, logger)
        }
    }

    @GetMapping("{id}/history")
    fun getHistory(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()
        return try {
            ResponseEntity.ok(hourCorridorService.getAuditHistory(id))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getHistory", startMs, logger)
        }
    }

    @GetMapping("{id}/assistance-plans")
    fun getAssistancePlans(@PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(hourCorridorService.getAssistancePlans(id))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        }
    }
}
