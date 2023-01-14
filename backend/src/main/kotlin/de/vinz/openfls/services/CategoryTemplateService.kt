package de.vinz.openfls.services

import de.vinz.openfls.model.Category
import de.vinz.openfls.model.CategoryTemplate
import de.vinz.openfls.repositories.CategoryTemplateRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import javax.transaction.Transactional

@Service
class CategoryTemplateService(
    private val categoryTemplateRepository: CategoryTemplateRepository,
    private val categoryServiceImpl: CategoryService
) : GenericService<CategoryTemplate> {

    @Transactional
    override fun create(value: CategoryTemplate): CategoryTemplate {
        // backup categories
        val categories = value.categories ?: mutableSetOf()
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

        return entity
    }

    @Transactional
    override fun update(value: CategoryTemplate): CategoryTemplate {
        if (!categoryTemplateRepository.existsById(value.id ?: 0))
            throw IllegalArgumentException("id not found")

        // backup categories
        val categories = value.categories ?: mutableSetOf()
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

        return entity
    }

    @Transactional
    override fun delete(id: Long) {
        return categoryTemplateRepository.deleteById(id)
    }

    override fun getAll(): List<CategoryTemplate> {
        return categoryTemplateRepository.findAllByTitle().toList()
    }

    override fun getById(id: Long): CategoryTemplate? {
        return categoryTemplateRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return categoryTemplateRepository.existsById(id)
    }
}