package de.vinz.openfls.domains.absence
import de.vinz.openfls.logging.StructuredLog

import de.vinz.openfls.domains.absence.dtos.AbsenceCreateRequest
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.services.ServiceController
import de.vinz.openfls.logback.PerformanceLogbackFilter
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/absences")
class AbsenceController(
    private val absenceService: AbsenceService,
    private val accessService: AccessService
) {

    private val logger: Logger = LoggerFactory.getLogger(ServiceController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody request: AbsenceCreateRequest): Any {
        return try {
            val startMs = System.currentTimeMillis()

            val response = absenceService.create(request)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms for employee %d",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    response.employeeId))
            }

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @DeleteMapping("/{date}")
    fun remove(@PathVariable date: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()

            absenceService.delete(date)

            if (logPerformance) {
                logger.info(String.format("%s remove took %s ms for employee %d",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    accessService.getId()))
            }

            ResponseEntity.ok().build<Any>()
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping
    fun getAll(): Any {
        return try {
            val startMs = System.currentTimeMillis()

            val response = absenceService.getAllByEmployeeId(accessService.getId())

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms and found %d absences",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    response.absenceDates.size))
            }

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

}