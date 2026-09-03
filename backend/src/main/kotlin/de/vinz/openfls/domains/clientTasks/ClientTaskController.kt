package de.vinz.openfls.domains.clientTasks

import de.vinz.openfls.domains.clientTasks.dtos.CompleteClientTaskDto
import de.vinz.openfls.domains.clientTasks.dtos.CreateClientTaskDto
import de.vinz.openfls.domains.clientTasks.dtos.UpdateClientTaskDto
import de.vinz.openfls.domains.clientTasks.exceptions.InvalidClientTaskException
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Each fachliche task action has its own endpoint. Tasks can be created, changed,
 * completed and deleted by authenticated employees; a completed task cannot be
 * changed or reopened. The detail history exposes changes and completions only.
 */
@RestController
@RequestMapping("/client_tasks")
class ClientTaskController(
    private val clientTaskService: ClientTaskService,
    private val clientService: ClientService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientTaskController::class.java)

    @GetMapping("client/{clientId}")
    fun getByClientId(@PathVariable clientId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            if (!clientService.existsById(clientId))
                throw InvalidClientTaskException("client not found")

            ResponseEntity.ok(clientTaskService.getDtosByClientId(clientId))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getByClientId", startMs, logger)
        }
    }

    @GetMapping("client/{clientId}/completed")
    fun getCompletedByClientId(
        @PathVariable clientId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Any {
        val startMs = System.currentTimeMillis()
        return try {
            if (!clientService.existsById(clientId)) throw InvalidClientTaskException("client not found")
            ResponseEntity.ok(clientTaskService.getCompletedDtosByClientId(clientId, page, size))
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getCompletedByClientId", startMs, logger)
        }
    }

    @PostMapping
    fun create(@Valid @RequestBody valueDto: CreateClientTaskDto): Any {
        val startMs = System.currentTimeMillis()

        return try {
            if (!clientService.existsById(valueDto.clientId))
                throw InvalidClientTaskException("client not found")

            ResponseEntity.ok(clientTaskService.create(valueDto, actorId(), actorName()))
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

    @PutMapping("{id}/change")
    fun update(@PathVariable id: Long, @Valid @RequestBody valueDto: UpdateClientTaskDto): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(clientTaskService.update(id, valueDto, actorId(), actorName()))
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

    @PostMapping("{id}/complete")
    fun complete(@PathVariable id: Long, @Valid @RequestBody valueDto: CompleteClientTaskDto): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(clientTaskService.complete(id, valueDto, actorId(), actorName()))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("complete", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val dto = clientTaskService.getDtoById(id)
            clientTaskService.delete(id, actorId(), actorName())
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

    @GetMapping("{id}/history")
    fun getHistory(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            if (!clientTaskService.existsById(id)) throw InvalidClientTaskException("client task not found")
            ResponseEntity.ok(clientTaskService.getAuditHistory(id))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getHistory", startMs, logger)
        }
    }

    private fun actorId(): Long = accessService.getId()

    private fun actorName(): String {
        val employee = employeeService.getEmployeeDtoById(actorId(), false)
            ?: return "Unbekannt"
        return "${employee.firstName} ${employee.lastName}".trim().ifBlank { "Unbekannt" }
    }
}
