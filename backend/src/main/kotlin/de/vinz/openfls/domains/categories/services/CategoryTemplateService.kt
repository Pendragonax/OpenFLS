package de.vinz.openfls.domains.categories.services

import de.vinz.openfls.domains.categories.dtos.CategoryDto
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.services.GenericService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper

@Service
class CategoryTemplateService(
        private val categoryTemplateRepository: CategoryTemplateRepository,
        private val categoryServiceImpl: CategoryService,
        private val modelMapper: ModelMapper
) : GenericService<CategoryTemplate> {

    private val logger: Logger = LoggerFactory.getLogger(CategoryTemplateService::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @Transactional
    fun create(valueDto: CategoryTemplateDto): CategoryTemplateDto {
        val entity = create(modelMapper.map(valueDto, CategoryTemplate::class.java))

        // generate return DTO
        valueDto.apply {
            id = entity.id ?: throw IllegalArgumentException("id not found after saving")
            categories = entity.categories
                    .map { modelMapper.map(it, CategoryDto::class.java) }
                    .sortedBy { id }
                    .toTypedArray()
        }

        return valueDto
    }

    @Transactional
    override fun create(value: CategoryTemplate): CategoryTemplate {
        // performance
        val startMs = System.currentTimeMillis()

        // backup categories
        val categories = value.categories
        value.categories = mutableSetOf()

        // save entity
        val entity = categoryTemplateRepository.save(value)

        // add / update categories
        entity.categories = categories
            .map { category: Category ->
                categoryServiceImpl.create(category.apply {
                    this.categoryTemplate = entity
                })}
            .toMutableSet()

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    @Transactional
    fun update(valueDto: CategoryTemplateDto): CategoryTemplateDto {
        val entity = update(modelMapper.map(valueDto, CategoryTemplate::class.java))

        valueDto.categories = entity.categories
                .map { modelMapper.map(it, CategoryDto::class.java) }
                .sortedBy { it.id }
                .toTypedArray()

        return valueDto
    }


    @Transactional
    override fun update(value: CategoryTemplate): CategoryTemplate {
        // performance
        val startMs = System.currentTimeMillis()

        if (!categoryTemplateRepository.existsById(value.id ?: 0))
            throw IllegalArgumentException("id not found")

        // backup categories
        val categories = value.categories
        value.categories = mutableSetOf()

        // save entity
        val entity = categoryTemplateRepository.save(value)

        // delete categories
        categoryServiceImpl
            .getAllByTemplateId(entity.id ?: 0)
            .filter { !categories.any { category -> category.id == it.id } }
            .forEach { categoryServiceImpl.delete(it.id!!) }

        // add / update categories
        entity.categories = categories
            .map { category: Category ->
                categoryServiceImpl.create(category.apply {
                    this.categoryTemplate = entity
                })}
            .toMutableSet()

        if (logPerformance) {
            logger.info(String.format("%s update took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    @Transactional
    override fun delete(id: Long) {
        // performance
        val startMs = System.currentTimeMillis()

        val result = categoryTemplateRepository.deleteById(id)

        if (logPerformance) {
            logger.info(String.format("%s delete took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return result
    }

    fun getAllDtos(): List<CategoryTemplateDto> {
        return getAll()
                .sortedByDescending { it.title.lowercase() }
                .map { value ->
                    modelMapper.map(value, CategoryTemplateDto::class.java).apply {
                        categories = categories.sortedBy { it.id }.toTypedArray() }
                }
    }

    override fun getAll(): List<CategoryTemplate> {
        // performance
        val startMs = System.currentTimeMillis()

        val result = categoryTemplateRepository.findAllByTitle().toList()

        if (logPerformance) {
            logger.info(String.format("%s getAll took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return result
    }

    fun getDtoById(id: Long): CategoryTemplateDto? {
        return modelMapper.map(getById(id), CategoryTemplateDto::class.java)
                .apply { categories = categories.sortedBy { it.id }.toTypedArray() }
    }

    override fun getById(id: Long): CategoryTemplate? {
        // performance
        val startMs = System.currentTimeMillis()

        val result = categoryTemplateRepository.findByIdOrNull(id)

        if (logPerformance) {
            logger.info(String.format("%s getById took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return result
    }

    override fun existsById(id: Long): Boolean {
        // performance
        val startMs = System.currentTimeMillis()

        val result = categoryTemplateRepository.existsById(id)

        if (logPerformance) {
            logger.info(String.format("%s existsById took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return result
    }
}