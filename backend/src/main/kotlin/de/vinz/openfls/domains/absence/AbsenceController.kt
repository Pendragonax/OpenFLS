package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.absence.dto.AbsenceCreateRequest
import de.vinz.openfls.domains.absence.service.AbsenceService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/absences")
class AbsenceController(
    private val absenceService: AbsenceService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(AbsenceController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: AbsenceCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(absenceService.create(request))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @DeleteMapping("/{date}")
    fun remove(@PathVariable date: LocalDate): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            absenceService.delete(date)
            ResponseEntity.ok().build<Any>()
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("remove", startMs, logger)
        }
    }

    @GetMapping
    fun getAll(): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(absenceService.getAllByEmployeeId(accessService.getId()))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

}
