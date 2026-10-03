package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.institutions.dto.InstitutionCreateRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionDeleteResult
import de.vinz.openfls.domains.institutions.dto.InstitutionUpdateRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionCreateResult
import de.vinz.openfls.domains.institutions.dto.InstitutionUpdateResult
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/institutions")
class InstitutionController(
        private val institutionService: InstitutionService,
        private val accessService: AccessService,
        private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(InstitutionController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: InstitutionCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = institutionService.create(request)) {
                is InstitutionCreateResult.Success -> ResponseEntity.ok(result.response)
                InstitutionCreateResult.EmployeeNotFound -> employeeNotFound()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody request: InstitutionUpdateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")

        return try {
            when (val result = institutionService.update(request)) {
                is InstitutionUpdateResult.Success -> ResponseEntity.ok(result.response)
                InstitutionUpdateResult.NotFound -> institutionNotFound()
                InstitutionUpdateResult.EmployeeNotFound -> employeeNotFound()
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

        return try {
            when (val result = institutionService.delete(id)) {
                is InstitutionDeleteResult.Success -> ResponseEntity.ok(result.response)
                InstitutionDeleteResult.NotFound -> institutionNotFound()
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
            ResponseEntity.ok(institutionService.getAllWithPermissions())
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

    @GetMapping("/readable")
    fun getAllReadable(): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(institutionService.getAll().filter { accessService.canReadEntries(it.id) })
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAllReadable", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            val response = institutionService.getWithPermissionsById(id) ?: return institutionNotFound()
            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    private fun employeeNotFound(): ResponseEntity<String> =
        ResponseEntity.badRequest().body("employee not found")

    private fun institutionNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("institution not found")
}
