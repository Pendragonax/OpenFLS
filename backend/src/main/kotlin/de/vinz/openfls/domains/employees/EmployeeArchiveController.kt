package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.dto.EmployeeArchiveActionRequest
import de.vinz.openfls.domains.employees.dto.EmployeeArchiveResult
import de.vinz.openfls.domains.employees.service.EmployeeArchiveService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees")
class EmployeeArchiveController(
    private val employeeArchiveService: EmployeeArchiveService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {
    private val logger: Logger = LoggerFactory.getLogger(EmployeeArchiveController::class.java)

    @GetMapping("{id}/archive/history")
    fun getHistory(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to read the archive history")

        return try {
            val history = employeeArchiveService.getHistory(id)
                ?: return notFound()
            ResponseEntity.ok(history)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getHistory", startMs, logger)
        }
    }

    @PostMapping("{id}/archive")
    fun archive(@PathVariable id: Long, @Valid @RequestBody request: EmployeeArchiveActionRequest): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to archive employees")

        return try {
            toResponse(
                employeeArchiveService.archive(id, request.actionDate!!, request.reason, request.remark)
            )
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("archive", startMs, logger)
        }
    }

    @PostMapping("{id}/reactivate")
    fun reactivate(@PathVariable id: Long, @Valid @RequestBody request: EmployeeArchiveActionRequest): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to reactivate employees")

        return try {
            toResponse(
                employeeArchiveService.reactivate(id, request.actionDate!!, request.reason, request.remark)
            )
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("reactivate", startMs, logger)
        }
    }

    private fun toResponse(result: EmployeeArchiveResult): ResponseEntity<out Any> {
        return when (result) {
            is EmployeeArchiveResult.Success -> ResponseEntity.ok(result.response)
            EmployeeArchiveResult.NotFound -> notFound()
            EmployeeArchiveResult.ActorNotFound -> ResponseEntity.badRequest().body("executing employee not found")
            EmployeeArchiveResult.AlreadyArchived -> conflict("employee already archived")
            EmployeeArchiveResult.NotArchived -> conflict("employee is not archived")
        }
    }

    private fun notFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("employee not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)

    private fun conflict(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(message)
}
