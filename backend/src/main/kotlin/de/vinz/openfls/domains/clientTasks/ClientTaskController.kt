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
 * Client tasks are readable, creatable and completable by every authenticated
 * employee. Deleting a task and reading its audit history stay restricted, because
 * both touch documented history.
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

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody valueDto: UpdateClientTaskDto): Any {
        val startMs = System.currentTimeMillis()

        return try {
            if (id != valueDto.id) throw InvalidClientTaskException("path id and dto id are not the same")
            ResponseEntity.ok(clientTaskService.update(valueDto, actorId(), actorName()))
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

    @PostMapping("{id}/reopen")
    fun reopen(@PathVariable id: Long, @RequestBody(required = false) valueDto: CompleteClientTaskDto?): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(clientTaskService.reopen(id, valueDto?.comment ?: "", actorId(), actorName()))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("reopen", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            if (!accessService.isAdmin())
                throw IllegalAccessException("no permission to delete client tasks")

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
            val clientId = clientTaskService.getClientIdById(id)
            val institutionId = clientService.getDtoById(
                id = clientId,
                includeArchived = true,
                leadingInstitutionIds = accessService.getLeadingInstitutionIds()
            )?.institution?.id ?: 0

            if (!accessService.isAdmin() && !accessService.isLeader(institutionId))
                throw IllegalAccessException("no permission to read the audit history of client tasks")

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
