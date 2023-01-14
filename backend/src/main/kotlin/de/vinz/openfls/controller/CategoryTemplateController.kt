package de.vinz.openfls.controller

import de.vinz.openfls.dtos.CategoryDto
import de.vinz.openfls.dtos.CategoryTemplateDto
import de.vinz.openfls.model.CategoryTemplate
import de.vinz.openfls.services.CategoryTemplateService
import de.vinz.openfls.services.HelperService
import org.modelmapper.ModelMapper
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
    private val helperService: HelperService,
    private val modelMapper: ModelMapper
) {
    @PostMapping
    fun create(@Valid @RequestBody valueDto: CategoryTemplateDto): Any {
        return try {
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

            helperService.printLog(this::class.simpleName, "create [id=${entity.id}]", false)

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "create - ${ex.message}", true)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: CategoryTemplateDto): Any {
        return try {
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

            helperService.printLog(this::class.simpleName, "update [id=${entity.id}]", false)

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "update [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            if (!categoryTemplateService.existsById(id))
                throw IllegalArgumentException("category template not found")

            val dto = modelMapper.map(categoryTemplateService.getById(id), CategoryTemplateDto::class.java)
            categoryTemplateService.delete(id)

            helperService.printLog(this::class.simpleName, "delete [id=${dto.id}]", false)

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "delete [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            val entities = categoryTemplateService
                .getAll()
                .sortedByDescending { it.title }
                .map { value ->
                    modelMapper.map(value, CategoryTemplateDto::class.java).apply {
                        categories = categories.sortedBy { it.id }.toTypedArray() }
                }

            helperService.printLog(this::class.simpleName, "getAll", false)

            ResponseEntity.ok(entities)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getAll - ${ex.message}", true)

            ResponseEntity.ok(emptyList())
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            val dto = modelMapper.map(categoryTemplateService.getById(id), CategoryTemplateDto::class.java)
                .apply { categories = categories.sortedBy { it.id }.toTypedArray() }

            helperService.printLog(this::class.simpleName, "getById", false)

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById - ${ex.message}", true)

            ResponseEntity.ok(null)
        }
    }
}