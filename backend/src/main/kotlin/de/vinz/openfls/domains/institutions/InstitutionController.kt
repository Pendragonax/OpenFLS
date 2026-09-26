package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.institutions.dtos.InstitutionCreateRequest
import de.vinz.openfls.domains.institutions.dtos.InstitutionUpdateRequest
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.logging.StructuredLog
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/institutions")
class InstitutionController(
        private val institutionService: InstitutionService,
        private val accessService: AccessService
) {

    private val logger: Logger = LoggerFactory.getLogger(InstitutionController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody request: InstitutionCreateRequest): Any {
        return try {
            val startMs = System.currentTimeMillis()

            val response = institutionService.create(request)

            logPerformance("create", startMs)

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody request: InstitutionUpdateRequest): Any {
        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")
        if (!institutionService.existsById(id))
            return institutionNotFound()

        return try {
            val startMs = System.currentTimeMillis()

            val response = institutionService.update(request)

            logPerformance("update", startMs)

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        val institution = institutionService.getWithPermissionsById(id) ?: return institutionNotFound()

        return try {
            val startMs = System.currentTimeMillis()

            institutionService.delete(id)

            logPerformance("delete", startMs)

            ResponseEntity.ok(institution)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            val startMs = System.currentTimeMillis()

            val response = institutionService.getAllWithPermissions()

            logPerformance("getAll", startMs)

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                emptyList<Any>(),
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/readable")
    fun getAllReadable(): Any {
        return try {
            val startMs = System.currentTimeMillis()

            val response = institutionService.getAll().filter { accessService.canReadEntries(it.id) }

            logPerformance("getAllReadable", startMs)

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                emptyList<Any>(),
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()

            val response = institutionService.getWithPermissionsById(id) ?: return institutionNotFound()

            logPerformance("getById", startMs)

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    private fun institutionNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("institution not found")

    private fun logPerformance(operation: String, startMs: Long) {
        if (logPerformance) {
            logger.info(String.format("%s %s took %s ms",
                PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                operation,
                System.currentTimeMillis() - startMs))
        }
    }
}
