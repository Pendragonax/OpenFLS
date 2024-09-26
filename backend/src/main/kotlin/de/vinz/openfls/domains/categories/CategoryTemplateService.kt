package de.vinz.openfls.domains.categories

import de.vinz.openfls.domains.categories.dtos.CategoryTemplateDto
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.exceptions.InvalidCategoryTemplateDtoException
import de.vinz.openfls.domains.categories.repositories.CategoryRepository
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CategoryTemplateService(private val categoryTemplateRepository: CategoryTemplateRepository,
                              private val categoryRepository: CategoryRepository) {

    @Transactional
    fun create(valueDto: CategoryTemplateDto): CategoryTemplateDto {
        val entity = categoryTemplateRepository.save(CategoryTemplate.soloFrom(valueDto))

        val categoriesWithCorrectTemplate = valueDto.categories
                .map { Category.from(it).apply { categoryTemplate = entity } }
                .toMutableSet()
        entity.categories = categoriesWithCorrectTemplate

        categoryTemplateRepository.save(entity)

        return CategoryTemplateDto.from(entity)
    }

    @Transactional
    fun update(valueDto: CategoryTemplateDto): CategoryTemplateDto {
        val existingTemplate = categoryTemplateRepository.findById(valueDto.id)

        if (existingTemplate.isEmpty) {
            throw InvalidCategoryTemplateDtoException("id not found")
        }

        // delete categories
        val existingCategories = existingTemplate.get().categories
        val categoriesToDelete = existingCategories.filter { existing -> valueDto.categories.none { it.id == existing.id } }
        categoriesToDelete.forEach { categoryRepository.deleteById(it.id) }

        val newTemplate = categoryTemplateRepository.save(CategoryTemplate.from(valueDto))
        return CategoryTemplateDto.from(newTemplate)
    }

    @Transactional
    fun delete(id: Long) {
        categoryTemplateRepository.deleteById(id)
    }

    fun getAll(): List<CategoryTemplateDto> {
        val entities = categoryTemplateRepository.findAll()
        return entities.map { CategoryTemplateDto.from(it) }.sortedBy { it.title }
    }

    fun getDtoById(id: Long): CategoryTemplateDto? {
        val entity = categoryTemplateRepository.findById(id).orElse(null)
        return entity?.let { CategoryTemplateDto.from(it) }
    }

    fun getById(id: Long): CategoryTemplate? {
        return categoryTemplateRepository.findByIdOrNull(id)
    }

    fun existsById(id: Long): Boolean {
        return categoryTemplateRepository.existsById(id)
    }
}