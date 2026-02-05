package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.services.ContingentCalendarService
import de.vinz.openfls.domains.contingents.services.ContingentEvaluationService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/contingents/evaluations")
class ContingentEvaluationController(
    private val contingentEvaluationService: ContingentEvaluationService,
    private val performanceLoggingService: PerformanceLoggingService,
    private val accessService: AccessService,
    private val contingentCalendarService: ContingentCalendarService,
) {

    private val logger: Logger = LoggerFactory.getLogger(ContingentEvaluationController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @GetMapping("institution/{institutionId}/{year}")
    fun getByInstitution(@PathVariable institutionId: Long, @PathVariable year: Int): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            val contingentEvaluation =
                contingentEvaluationService.generateContingentEvaluationFor(year, institutionId)
            return ResponseEntity.ok(contingentEvaluation)
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getByInstitutionId", startMs, logger)
        }
    }

    @GetMapping("employee/{id}/{end}")
    fun getTimes2ByEmployee(@PathVariable id: Long,
                            @Valid @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (accessService.getId() != id &&
                !accessService.isAdmin() &&
                !accessService.canReadEmployee(id))
                throw IllegalArgumentException("No permission to get the times of this employee")

            val calendarDto = contingentCalendarService.generateContingentCalendarInformationFor(id, end)

            if (logPerformance) {
                logger.info(String.format("%s getTimesByEmployee took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(calendarDto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}