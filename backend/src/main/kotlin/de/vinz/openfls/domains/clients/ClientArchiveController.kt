package de.vinz.openfls.domains.clients
import de.vinz.openfls.logging.StructuredLog

import de.vinz.openfls.domains.clients.archive.ClientArchiveActionRequest
import de.vinz.openfls.domains.clients.archive.ClientArchiveActor
import de.vinz.openfls.domains.clients.archive.ClientArchiveService
import de.vinz.openfls.domains.clients.archive.ClientArchiveStateException
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportFormat
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportService
import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportStateException
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportRequestDto
import de.vinz.openfls.domains.clients.archive.export.dtos.ClientArchiveExportStatusDto
import de.vinz.openfls.domains.clients.archive.dtos.ClientArchiveHistoryEntryReadDto
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.exceptions.UserNotAllowedException
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clients")
class ClientArchiveController(
    private val clientArchiveService: ClientArchiveService,
    private val clientArchiveExportService: ClientArchiveExportService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientArchiveController::class.java)

    @GetMapping("{id}/archive/history")
    fun getArchiveHistory(@PathVariable id: Long): ResponseEntity<List<ClientArchiveHistoryEntryReadDto>> {
        return ResponseEntity.ok(clientArchiveService.getArchiveHistory(id))
    }

    @PostMapping("{id}/archive")
    fun archive(
        @PathVariable id: Long,
        @Valid @RequestBody request: ClientArchiveActionRequest
    ): Any {
        return try {
            ResponseEntity.ok(
                clientArchiveService.archive(
                    clientId = id,
                    actionDate = request.actionDate!!,
                    reason = request.reason,
                    remark = request.remark,
                    actor = loadActor()
                )
            )
        } catch (ex: UserNotAllowedException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
        } catch (ex: ClientArchiveStateException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.CONFLICT)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("{id}/reactivate")
    fun reactivate(
        @PathVariable id: Long,
        @Valid @RequestBody request: ClientArchiveActionRequest
    ): Any {
        return try {
            ResponseEntity.ok(
                clientArchiveService.reactivate(
                    clientId = id,
                    actionDate = request.actionDate!!,
                    reason = request.reason,
                    remark = request.remark,
                    actor = loadActor()
                )
            )
        } catch (ex: UserNotAllowedException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
        } catch (ex: ClientArchiveStateException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.CONFLICT)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}/archive/export")
    fun getExportStatus(@PathVariable id: Long): ResponseEntity<ClientArchiveExportStatusDto> {
        return try {
            ResponseEntity.ok(clientArchiveExportService.getExportStatus(id, loadActor()))
        } catch (ex: UserNotAllowedException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PostMapping("{id}/archive/export")
    fun requestExport(
        @PathVariable id: Long,
        @Valid @RequestBody request: ClientArchiveExportRequestDto
    ): Any {
        return try {
            ResponseEntity.ok(
                clientArchiveExportService.requestExport(
                    clientId = id,
                    format = request.format ?: ClientArchiveExportFormat.JSON,
                    anonymize = request.anonymize,
                    actor = loadActor()
                )
            )
        } catch (ex: UserNotAllowedException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
        } catch (ex: ClientArchiveExportStateException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.CONFLICT)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}/archive/export/{downloadToken}")
    fun downloadExport(
        @PathVariable id: Long,
        @PathVariable downloadToken: String
    ): Any {
        return try {
            val download = clientArchiveExportService.downloadExport(id, downloadToken)
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${download.fileName}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ByteArrayResource(download.content))
        } catch (ex: ClientArchiveExportStateException) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.GONE)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
        }
    }

    private fun loadActor(): ClientArchiveActor {
        val employeeId = accessService.getId()
        val employee = employeeService.getEmployeeDtoById(employeeId, accessService.isAdmin())
            ?: throw IllegalArgumentException("employee not found")

        return ClientArchiveActor(
            employeeId = employee.id,
            firstname = employee.firstName,
            lastname = employee.lastName,
            isAdmin = accessService.isAdmin(),
            leadingInstitutionIds = accessService.getLeadingInstitutionIds()
        )
    }
}
