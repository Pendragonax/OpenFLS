package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.absence.dtos.CreateAbsenceDTO
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
    fun create(@Valid @RequestBody createAbsenceDTO: CreateAbsenceDTO): Any {
        return try {
            val startMs = System.currentTimeMillis()

            val dto = absenceService.create(createAbsenceDTO.absenceDate)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms for employee %d",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    dto.employeeId))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

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

            absenceService.remove(date)

            if (logPerformance) {
                logger.info(String.format("%s remove took %s ms for employee %d",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    accessService.getId()))
            }

            ResponseEntity.ok().build<Any>()
        } catch (ex: Exception) {
            logger.error(ex.message)

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

            val dto = absenceService.getAllByEmployeeId(accessService.getId())

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms and found %d absences",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    dto.absenceDates.size))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

}