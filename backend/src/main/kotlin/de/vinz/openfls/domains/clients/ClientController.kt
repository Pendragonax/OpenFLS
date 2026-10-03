package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dto.ClientCreateRequest
import de.vinz.openfls.domains.clients.dto.ClientCreateResult
import de.vinz.openfls.domains.clients.dto.ClientDeleteResult
import de.vinz.openfls.domains.clients.dto.ClientUpdateRequest
import de.vinz.openfls.domains.clients.dto.ClientUpdateResult
import de.vinz.openfls.domains.clients.service.ClientDeletionService
import de.vinz.openfls.domains.clients.service.ClientForServiceEditingService
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientService: ClientService,
    private val clientDeletionService: ClientDeletionService,
    private val clientForServiceEditingService: ClientForServiceEditingService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: ClientCreateRequest): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isLeader(request.institutionId))
            return forbidden("no permission to add clients")

        return try {
            when (val result = clientService.create(request)) {
                is ClientCreateResult.Success -> ResponseEntity.ok(result.response)
                ClientCreateResult.InstitutionNotFound -> badRequest("institution not found")
                ClientCreateResult.CategoryTemplateNotFound -> badRequest("category template not found")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: ClientUpdateRequest): Any {
        val startMs = System.currentTimeMillis()

        if (id != request.id)
            return badRequest("path id and request id are not the same")
        if (!accessService.canModifyClient(request.id))
            return forbidden("no permission to update this client")

        return try {
            when (val result = clientService.update(request)) {
                is ClientUpdateResult.Success -> ResponseEntity.ok(result.response)
                ClientUpdateResult.NotFound -> notFound()
                ClientUpdateResult.Archived -> conflict("client is archived")
                ClientUpdateResult.InstitutionNotFound -> badRequest("institution not found")
                ClientUpdateResult.CategoryTemplateNotFound -> badRequest("category template not found")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to delete this client")

        return try {
            when (val result = clientDeletionService.delete(id)) {
                is ClientDeleteResult.Success -> ResponseEntity.ok(result.response)
                ClientDeleteResult.NotFound -> notFound()
                ClientDeleteResult.Archived -> conflict("client is archived")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping
    fun getAll(): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(
                clientService.getAllClientsWithInstitution(
                    includeArchived = accessService.isAdmin(),
                    leadingInstitutionIds = accessService.getLeadingInstitutionIds()
                )
            )
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val client = clientService.getById(
                id,
                includeArchived = accessService.isAdmin(),
                leadingInstitutionIds = accessService.getLeadingInstitutionIds()
            ) ?: return notFound()
            ResponseEntity.ok(client)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    @GetMapping("/for-service-editing/{id}")
    fun getForServiceEditingById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val client = clientForServiceEditingService.getForServiceEditingById(id) ?: return notFound()
            ResponseEntity.ok(client)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getForServiceEditingById", startMs, logger)
        }
    }

    private fun notFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("client not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)

    private fun badRequest(message: String): ResponseEntity<String> =
        ResponseEntity.badRequest().body(message)

    private fun conflict(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(message)
}
