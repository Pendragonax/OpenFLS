package de.vinz.openfls.controller

import de.vinz.openfls.dtos.CategoryDto
import de.vinz.openfls.dtos.CategoryTemplateDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.model.CategoryTemplate
import de.vinz.openfls.services.CategoryTemplateService
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.validation.Valid

@RestController
@RequestMapping("/categories")
class CategoryTemplateController(
    private val categoryTemplateService: CategoryTemplateService,
    private val modelMapper: ModelMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(CategoryTemplateController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody valueDto: CategoryTemplateDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = categoryTemplateService.create(modelMapper.map(valueDto, CategoryTemplate::class.java))

            // generate return DTO
            valueDto.apply {
                id = entity.id ?: throw IllegalArgumentException("id not found after saving")
                categories = entity.categories
                    ?.map { modelMapper.map(it, CategoryDto::class.java) }
                    ?.sortedBy { id }
                    ?.toTypedArray()
                    ?: emptyArray()
            }

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: CategoryTemplateDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (id != valueDto.id)
                throw IllegalArgumentException("path id and dto id are not the same")
            if (!categoryTemplateService.existsById(id))
                throw IllegalArgumentException("category template not found")

            val entity = categoryTemplateService.update(modelMapper.map(valueDto, CategoryTemplate::class.java))

            valueDto.categories = entity.categories
                ?.map { modelMapper.map(it, CategoryDto::class.java) }
                ?.sortedBy { it.id }
                ?.toTypedArray()
                ?: emptyArray()

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!categoryTemplateService.existsById(id))
                throw IllegalArgumentException("category template not found")

            val dto = modelMapper.map(categoryTemplateService.getById(id), CategoryTemplateDto::class.java)
            categoryTemplateService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entities = categoryTemplateService
                .getAll()
                .sortedByDescending { it.title.lowercase() }
                .map { value ->
                    modelMapper.map(value, CategoryTemplateDto::class.java).apply {
                        categories = categories.sortedBy { it.id }.toTypedArray() }
                }

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(entities)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity.ok(emptyList())
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = modelMapper.map(categoryTemplateService.getById(id), CategoryTemplateDto::class.java)
                .apply { categories = categories.sortedBy { it.id }.toTypedArray() }

            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity.ok(null)
        }
    }
}