package de.vinz.openfls.domains.categories

import de.vinz.openfls.domains.categories.dtos.CategoryTemplateDto
import de.vinz.openfls.domains.categories.exceptions.InvalidCategoryTemplateDtoException
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    fun create(@RequestBody valueDto: CategoryTemplateDto): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(categoryTemplateService.create(valueDto))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: CategoryTemplateDto): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != valueDto.id)
            throw InvalidCategoryTemplateDtoException("path id and dto id are not the same")
        if (!categoryTemplateService.existsById(id))
            throw InvalidCategoryTemplateDtoException("category template not found")

        return try {
            ResponseEntity.ok(categoryTemplateService.update(valueDto))
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

        if (!categoryTemplateService.existsById(id))
            throw InvalidCategoryTemplateDtoException("category template not found")

        return try {
            val dto = categoryTemplateService.getDtoById(id)
            categoryTemplateService.delete(id)

            ResponseEntity.ok(dto)
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
            ResponseEntity.ok(categoryTemplateService.getDtoById(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }
}