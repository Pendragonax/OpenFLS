package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dto.ClientArchiveExportDownloadResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportRequest
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportRequestResult
import de.vinz.openfls.domains.clients.dto.ClientArchiveExportStatusResult
import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.service.ClientArchiveExportService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/clients")
class ClientArchiveExportController(
    private val clientArchiveExportService: ClientArchiveExportService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientArchiveExportController::class.java)

    @GetMapping("{id}/archive/export")
    fun getExportStatus(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = clientArchiveExportService.getExportStatus(id)) {
                is ClientArchiveExportStatusResult.Success -> ResponseEntity.ok(result.response)
                ClientArchiveExportStatusResult.NotFound -> notFound()
                ClientArchiveExportStatusResult.Forbidden -> forbidden()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getExportStatus", startMs, logger)
        }
    }

    @PostMapping("{id}/archive/export")
    fun requestExport(@PathVariable id: Long, @Valid @RequestBody request: ClientArchiveExportRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (
                val result = clientArchiveExportService.requestExport(
                    clientId = id,
                    format = request.format ?: ClientArchiveExportFormat.JSON,
                    anonymize = request.anonymize
                )
            ) {
                is ClientArchiveExportRequestResult.Success -> ResponseEntity.ok(result.response)
                ClientArchiveExportRequestResult.NotFound -> notFound()
                ClientArchiveExportRequestResult.Forbidden -> forbidden()
                ClientArchiveExportRequestResult.UnsupportedFormat -> conflict("unsupported export format")
                ClientArchiveExportRequestResult.ActorNotFound ->
                    ResponseEntity.badRequest().body("executing employee not found")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("requestExport", startMs, logger)
        }
    }

    @GetMapping("{id}/archive/export/{downloadToken}")
    fun downloadExport(@PathVariable id: Long, @PathVariable downloadToken: String): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = clientArchiveExportService.downloadExport(id, downloadToken)) {
                is ClientArchiveExportDownloadResult.Success ->
                    ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${result.download.fileName}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ByteArrayResource(result.download.content))
                is ClientArchiveExportDownloadResult.Gone ->
                    ResponseEntity.status(HttpStatus.GONE).body(result.reason)
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("downloadExport", startMs, logger)
        }
    }

    private fun notFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("client not found")

    private fun forbidden(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body("no permission to export this client")

    private fun conflict(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(message)
}
