package de.vinz.openfls.domains.contingents
import de.vinz.openfls.logging.StructuredLog

import de.vinz.openfls.domains.contingents.service.ContingentCalendarService
import de.vinz.openfls.domains.contingents.service.ContingentEvaluationService
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
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
    private val employeeService: EmployeeService,
) {

    private val logger: Logger = LoggerFactory.getLogger(ContingentEvaluationController::class.java)

    @GetMapping("institution/{institutionId}/{year}")
    fun getByInstitution(
        @PathVariable institutionId: Long,
        @PathVariable year: Int,
        @RequestParam(defaultValue = "false") includeArchivedEmployees: Boolean
    ): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            val contingentEvaluation = contingentEvaluationService.generateContingentEvaluationFor(
                year,
                institutionId,
                includeArchivedEmployees = accessService.isAdmin() && includeArchivedEmployees
            )
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
        val employee = employeeService.getEmployeeDtoById(id, true)
        if (employee == null || employee.archived)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("employee not found")
        if (accessService.getId() != id &&
            !accessService.isAdmin() &&
            !accessService.canReadEmployee(id))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No permission to get the times of this employee")

        val startMs = System.currentTimeMillis()

        return try {
            val calendar = contingentCalendarService.generateContingentCalendarFor(id, end)

            ResponseEntity.ok(calendar)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        } finally {
            performanceLoggingService.logPerformance("getTimesByEmployee", startMs, logger)
        }
    }
}
