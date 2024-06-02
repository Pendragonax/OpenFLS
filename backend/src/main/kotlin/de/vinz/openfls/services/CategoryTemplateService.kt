package de.vinz.openfls.services

import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.entities.Category
import de.vinz.openfls.entities.CategoryTemplate
import de.vinz.openfls.repositories.CategoryTemplateRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import jakarta.transaction.Transactional

@Service
class CategoryTemplateService(
    private val categoryTemplateRepository: CategoryTemplateRepository,
    private val categoryServiceImpl: CategoryService
) : GenericService<CategoryTemplate> {

    private val logger: Logger = LoggerFactory.getLogger(CategoryTemplateService::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

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