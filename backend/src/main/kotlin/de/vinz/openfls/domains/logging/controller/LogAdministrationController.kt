package de.vinz.openfls.domains.logging.controller

import de.vinz.openfls.domains.logging.dto.LogLevelDto
import de.vinz.openfls.domains.logging.dto.LogPageDto
import de.vinz.openfls.domains.logging.dto.LogQueryDto
import de.vinz.openfls.domains.logging.dto.LogSettingsDto
import de.vinz.openfls.domains.logging.service.LogAdministrationService
import de.vinz.openfls.logging.StructuredLog
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/admin/logs")
class LogAdministrationController(private val logService: LogAdministrationService) {
    @GetMapping("/days") fun days(): List<String> = logService.availableDays().also { StructuredLog.audit("logging.days.read", "success") }
    @GetMapping fun entries(query: LogQueryDto) = logService.entries(query).also { StructuredLog.audit("logging.entries.read", "success") }
    @GetMapping("/page")
    fun page(
        query: LogQueryDto,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ): LogPageDto = logService.page(query, page, size).also { StructuredLog.audit("logging.entries.read", "success") }
    @GetMapping("/settings") fun settings(): LogSettingsDto = logService.settings().also { StructuredLog.audit("logging.settings.read", "success") }

    @PatchMapping("/settings") fun setLevel(@RequestBody value: LogLevelDto): LogSettingsDto {
        logService.setLevel(value.logger, value.level); StructuredLog.audit("logging.level.change", "success", "logger", value.logger); return logService.settings()
    }

    @DeleteMapping("/settings/{logger}") fun resetLevel(@PathVariable logger: String): LogSettingsDto {
        logService.resetLevel(logger); StructuredLog.audit("logging.level.reset", "success", "logger", logger); return logService.settings()
    }

    @DeleteMapping("/settings") fun resetLevels(): LogSettingsDto { logService.resetLevels(); StructuredLog.audit("logging.level.reset_all", "success"); return logService.settings() }
    @DeleteMapping fun delete(@RequestParam(required = false) from: String?) { logService.deleteFrom(from?.let(Instant::parse)); StructuredLog.audit("logging.technical.delete", "success") }

    @GetMapping("/export") fun export(query: LogQueryDto): ResponseEntity<StreamingResponseBody> = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=openhls-logs.zip")
        .contentType(MediaType.parseMediaType("application/zip"))
        .body(StreamingResponseBody { output -> logService.streamExport(query, output) })
        .also { StructuredLog.audit("logging.technical.export", "success") }
}
