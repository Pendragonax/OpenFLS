package de.vinz.openfls.domains.categories

import de.vinz.openfls.domains.categories.dtos.CategoryTemplateCreateRequest
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateUpdateRequest
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryTemplateController(
        private val categoryTemplateService: CategoryTemplateService,
        private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(CategoryTemplateController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: CategoryTemplateCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(categoryTemplateService.create(request))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody request: CategoryTemplateUpdateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")
        if (!categoryTemplateService.existsById(id))
            return categoryTemplateNotFound()

        return try {
            ResponseEntity.ok(categoryTemplateService.update(request))
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

        val categoryTemplate = categoryTemplateService.getById(id) ?: return categoryTemplateNotFound()

        return try {
            categoryTemplateService.delete(id)

            ResponseEntity.ok(categoryTemplate)
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
            ResponseEntity.ok(categoryTemplateService.getAll())
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
            val categoryTemplate = categoryTemplateService.getById(id) ?: return categoryTemplateNotFound()
            ResponseEntity.ok(categoryTemplate)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    private fun categoryTemplateNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("category template not found")
}
