package de.vinz.openfls.domains.categories

import de.vinz.openfls.domains.categories.dtos.CategoryTemplateCreateDto
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateUpdateDto
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateWithCategories
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.exceptions.InvalidCategoryTemplateDtoException
import de.vinz.openfls.domains.categories.repositories.CategoryRepository
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CategoryTemplateService(private val categoryTemplateRepository: CategoryTemplateRepository,
                              private val categoryRepository: CategoryRepository) {

    @Transactional
    fun create(valueDto: CategoryTemplateCreateDto): CategoryTemplateWithCategories {
        val entity = categoryTemplateRepository.save(CategoryTemplate.soloFrom(valueDto))

        val categoriesWithCorrectTemplate = valueDto.categories
                .map { Category.from(it).apply { categoryTemplate = entity } }
                .toMutableSet()
        entity.categories = categoriesWithCorrectTemplate

        categoryTemplateRepository.save(entity)

        return CategoryTemplateWithCategories.from(entity)
    }

    @Transactional
    fun update(valueDto: CategoryTemplateUpdateDto): CategoryTemplateWithCategories {
        val existingTemplate = categoryTemplateRepository.findById(valueDto.id)

        if (existingTemplate.isEmpty) {
            throw InvalidCategoryTemplateDtoException("id not found")
        }

        // delete categories
        val existingCategories = existingTemplate.get().categories
        val categoriesToDelete = existingCategories.filter { existing -> valueDto.categories.none { it.id == existing.id } }
        categoriesToDelete.forEach { categoryRepository.deleteById(it.id) }

        val newTemplate = categoryTemplateRepository.save(CategoryTemplate.from(valueDto))
        return CategoryTemplateWithCategories.from(newTemplate)
    }

    @Transactional
    fun delete(id: Long) {
        categoryTemplateRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<CategoryTemplateWithCategories> {
        val entities = categoryTemplateRepository.findAll()
        return entities.map { CategoryTemplateWithCategories.from(it) }.sortedBy { it.title }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): CategoryTemplateWithCategories? {
        val entity = categoryTemplateRepository.findById(id).orElse(null)
        return entity?.let { CategoryTemplateWithCategories.from(it) }
    }

    @Transactional(readOnly = true)
    fun getEntityById(id: Long): CategoryTemplate? {
        return categoryTemplateRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return categoryTemplateRepository.existsById(id)
    }
}
