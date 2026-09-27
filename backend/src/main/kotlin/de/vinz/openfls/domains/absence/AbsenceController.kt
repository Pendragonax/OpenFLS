package de.vinz.openfls.domains.absence
import de.vinz.openfls.logging.StructuredLog

import de.vinz.openfls.domains.absence.dtos.AbsenceCreateRequest
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.services.ServiceController
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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

    private val logger: Logger = LoggerFactory.getLogger(ServiceController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: AbsenceCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(absenceService.create(request))
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
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
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
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
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

}
