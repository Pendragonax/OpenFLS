package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.dto.ContingentCreateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentCreateResult
import de.vinz.openfls.domains.contingents.dto.ContingentDeleteResult
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateResult
import de.vinz.openfls.domains.contingents.service.ContingentService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/contingents")
class ContingentController(
    private val contingentService: ContingentService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ContingentController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: ContingentCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!accessService.isLeader(request.institutionId))
            return forbidden("no permission to add this contingent")

        return try {
            when (val result = contingentService.create(request)) {
                is ContingentCreateResult.Success -> ResponseEntity.ok(result.response)
                is ContingentCreateResult.InvalidRange -> ResponseEntity.badRequest().body(result.message)
                is ContingentCreateResult.EmployeeNotFound -> ResponseEntity.badRequest().body(result.message)
                is ContingentCreateResult.EmployeeArchived -> ResponseEntity.badRequest().body(result.message)
                is ContingentCreateResult.InstitutionNotFound -> ResponseEntity.badRequest().body(result.message)
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(
        @PathVariable id: Long, @Valid @RequestBody request: ContingentUpdateRequest
    ): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")
        if (contingentService.getById(id) == null)
            return contingentNotFound()
        if (!contingentService.canModifyContingent(id))
            return forbidden("no permission to update this contingent")

        return try {
            when (val result = contingentService.update(request)) {
                is ContingentUpdateResult.Success -> ResponseEntity.ok(result.response)
                ContingentUpdateResult.NotFound -> contingentNotFound()
                is ContingentUpdateResult.InvalidRange -> ResponseEntity.badRequest().body(result.message)
                is ContingentUpdateResult.EmployeeNotFound -> ResponseEntity.badRequest().body(result.message)
                is ContingentUpdateResult.EmployeeArchived -> ResponseEntity.badRequest().body(result.message)
                is ContingentUpdateResult.InstitutionNotFound -> ResponseEntity.badRequest().body(result.message)
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to delete this contingent")

        return try {
            when (val result = contingentService.delete(id)) {
                is ContingentDeleteResult.Success -> ResponseEntity.ok(result.response)
                ContingentDeleteResult.NotFound -> contingentNotFound()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(contingentService.getAll())
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            val contingent = contingentService.getById(id) ?: return contingentNotFound()
            ResponseEntity.ok(contingent)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    @GetMapping("employee/{id}")
    fun getByEmployeeId(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "false") includeArchivedEmployees: Boolean
    ): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(
                contingentService.getByEmployeeId(
                    id,
                    accessService.isAdmin() && includeArchivedEmployees
                )
            )
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getByEmployeeId", startMs, logger)
        }
    }

    @GetMapping("institution/{id}")
    fun getByInstitutionId(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "false") includeArchivedEmployees: Boolean
    ): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(
                contingentService.getByInstitutionId(
                    id,
                    accessService.isAdmin() && includeArchivedEmployees
                )
            )
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getByInstitutionId", startMs, logger)
        }
    }

    private fun contingentNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("contingent not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)
}
