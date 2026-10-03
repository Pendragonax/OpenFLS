package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dto.ClientArchiveActionRequest
import de.vinz.openfls.domains.clients.dto.ClientArchiveHistoryResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveResult
import de.vinz.openfls.domains.clients.service.ClientArchiveService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/clients")
class ClientArchiveController(
    private val clientArchiveService: ClientArchiveService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientArchiveController::class.java)

    @GetMapping("{id}/archive/history")
    fun getArchiveHistory(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = clientArchiveService.getHistory(id)) {
                is ClientArchiveHistoryResult.Success -> ResponseEntity.ok(result.entries)
                ClientArchiveHistoryResult.NotFound -> notFound()
                ClientArchiveHistoryResult.Forbidden -> forbidden("no permission to read the archive history")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getArchiveHistory", startMs, logger)
        }
    }

    @PostMapping("{id}/archive")
    fun archive(@PathVariable id: Long, @Valid @RequestBody request: ClientArchiveActionRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponse(clientArchiveService.archive(id, request.actionDate!!, request.reason, request.remark))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("archive", startMs, logger)
        }
    }

    @PostMapping("{id}/reactivate")
    fun reactivate(@PathVariable id: Long, @Valid @RequestBody request: ClientArchiveActionRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            toResponse(clientArchiveService.reactivate(id, request.actionDate!!, request.reason, request.remark))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("reactivate", startMs, logger)
        }
    }

    private fun toResponse(result: ClientArchiveResult): ResponseEntity<out Any> {
        return when (result) {
            is ClientArchiveResult.Success -> ResponseEntity.ok(result.response)
            ClientArchiveResult.NotFound -> notFound()
            ClientArchiveResult.Forbidden -> forbidden("no permission to archive or reactivate this client")
            ClientArchiveResult.ActorNotFound -> ResponseEntity.badRequest().body("executing employee not found")
            ClientArchiveResult.AlreadyArchived -> conflict("client already archived")
            ClientArchiveResult.NotArchived -> conflict("client is not archived")
        }
    }

    private fun notFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("client not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)

    private fun conflict(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(message)
}
