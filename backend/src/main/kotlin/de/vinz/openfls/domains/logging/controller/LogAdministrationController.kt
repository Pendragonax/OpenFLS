package de.vinz.openfls.domains.logging.controller

import de.vinz.openfls.domains.logging.dto.LogLevelDto
import de.vinz.openfls.domains.logging.dto.LogQueryDto
import de.vinz.openfls.domains.logging.dto.LogSettingsDto
import de.vinz.openfls.domains.logging.service.LogAdministrationService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    @GetMapping("/days") fun days(): List<String> = logService.availableDays()
    @GetMapping fun entries(query: LogQueryDto) = logService.entries(query)
    @GetMapping("/settings") fun settings(): LogSettingsDto = logService.settings()

    @PatchMapping("/settings") fun setLevel(@RequestBody value: LogLevelDto): LogSettingsDto {
        logService.setLevel(value.logger, value.level); return logService.settings()
    }

    @DeleteMapping("/settings/{logger}") fun resetLevel(@PathVariable logger: String): LogSettingsDto {
        logService.resetLevel(logger); return logService.settings()
    }

    @DeleteMapping("/settings") fun resetLevels(): LogSettingsDto { logService.resetLevels(); return logService.settings() }
    @DeleteMapping fun delete(@RequestParam(required = false) from: String?) { logService.deleteFrom(from?.let(Instant::parse)) }

    @GetMapping("/export") fun export(query: LogQueryDto): ResponseEntity<ByteArray> = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=openhls-logs.zip")
        .contentType(MediaType.parseMediaType("application/zip"))
        .body(logService.export(query))
}
