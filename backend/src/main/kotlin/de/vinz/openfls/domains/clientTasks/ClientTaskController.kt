package de.vinz.openfls.domains.clientTasks

import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCompletedPageResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskCreateResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskDeleteResult
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateRequest
import de.vinz.openfls.domains.clientTasks.dto.ClientTaskUpdateResult
import de.vinz.openfls.domains.clientTasks.service.ClientTaskService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientTaskController::class.java)

    @GetMapping("client/{clientId}")
    fun getOpenTasksByClientId(@PathVariable clientId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val tasks = clientTaskService.getOpenTasksByClientId(clientId)
                ?: return clientNotFound()
            ResponseEntity.ok(tasks)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getOpenTasksByClientId", startMs, logger)
        }
    }

    @GetMapping("client/{clientId}/completed")
    fun getCompletedTasksByClientId(
        @PathVariable clientId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = clientTaskService.getCompletedTasksByClientId(clientId, page, size)) {
                is ClientTaskCompletedPageResult.Success -> ResponseEntity.ok(result.response)
                ClientTaskCompletedPageResult.ClientNotFound -> clientNotFound()
                ClientTaskCompletedPageResult.InvalidPagination ->
                    ResponseEntity.badRequest().body("invalid pagination")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getCompletedTasksByClientId", startMs, logger)
        }
    }

    @PostMapping
    fun create(@Valid @RequestBody request: ClientTaskCreateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = clientTaskService.create(request)) {
                is ClientTaskCreateResult.Success -> ResponseEntity.ok(result.response)
                ClientTaskCreateResult.ClientNotFound -> clientNotFound()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}/change")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: ClientTaskUpdateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = clientTaskService.update(id, request)) {
                is ClientTaskUpdateResult.Success -> ResponseEntity.ok(result.response)
                ClientTaskUpdateResult.NotFound -> taskNotFound()
                ClientTaskUpdateResult.AlreadyCompleted -> alreadyCompleted("completed tasks cannot be changed")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
        }
    }

    @PostMapping("{id}/complete")
    fun complete(@PathVariable id: Long, @Valid @RequestBody request: ClientTaskCompleteRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = clientTaskService.complete(id, request)) {
                is ClientTaskCompleteResult.Success -> ResponseEntity.ok(result.response)
                ClientTaskCompleteResult.NotFound -> taskNotFound()
                ClientTaskCompleteResult.AlreadyCompleted -> alreadyCompleted("client task is already done")
            }
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
            when (val result = clientTaskService.delete(id)) {
                is ClientTaskDeleteResult.Success -> ResponseEntity.ok(result.response)
                ClientTaskDeleteResult.NotFound -> taskNotFound()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping("{id}/history")
    fun getAuditHistoryByTaskId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val history = clientTaskService.getAuditHistoryByTaskId(id)
                ?: return taskNotFound()
            ResponseEntity.ok(history)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAuditHistoryByTaskId", startMs, logger)
        }
    }

    private fun clientNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("client not found")

    private fun taskNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("client task not found")

    private fun alreadyCompleted(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(message)
}
