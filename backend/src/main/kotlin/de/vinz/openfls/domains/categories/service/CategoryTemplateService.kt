package de.vinz.openfls.domains.categories.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.categories.dto.CategoryTemplateCreateRequest
import de.vinz.openfls.domains.categories.dto.CategoryTemplateDeleteResult
import de.vinz.openfls.domains.categories.dto.CategoryTemplateUpdateRequest
import de.vinz.openfls.domains.categories.dto.CategoryTemplateUpdateResult
import de.vinz.openfls.domains.categories.dto.CategoryTemplateWithCategoriesResponse
import de.vinz.openfls.domains.categories.entity.Category
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryRepository
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryTemplateService(
    private val categoryTemplateRepository: CategoryTemplateRepository,
    private val categoryRepository: CategoryRepository
) {

    @Transactional
    fun create(request: CategoryTemplateCreateRequest): CategoryTemplateWithCategoriesResponse {
        val template = CategoryTemplate(
            title = request.title,
            description = request.description,
            withoutClient = request.withoutClient
        )
        template.categories = request.categories
            .map {
                Category(
                    title = it.title,
                    shortcut = it.shortcut,
                    description = it.description,
                    faceToFace = it.faceToFace,
                    categoryTemplate = template
                )
            }
            .toMutableSet()

        return CategoryTemplateWithCategoriesResponse.from(categoryTemplateRepository.save(template))
    }

    @Transactional
    fun update(request: CategoryTemplateUpdateRequest): CategoryTemplateUpdateResult {
        val template = categoryTemplateRepository.findByIdOrNull(request.id)
            ?: return CategoryTemplateUpdateResult.NotFound

        val existingIds = template.categories.map { it.id }.toSet()
        val foreignCategoryId = request.categories.firstOrNull { it.id > 0 && it.id !in existingIds }?.id
        if (foreignCategoryId != null) {
            return CategoryTemplateUpdateResult.CategoryNotInTemplate(
                "category with id $foreignCategoryId does not belong to category template ${template.id}"
            )
        }

        template.title = request.title
        template.description = request.description
        template.withoutClient = request.withoutClient

        val requestedIds = request.categories.filter { it.id > 0 }.map { it.id }.toSet()
        val categoriesToDelete = template.categories.filter { it.id !in requestedIds }
        template.categories.removeAll(categoriesToDelete.toSet())
        categoryRepository.deleteAll(categoriesToDelete)

        request.categories.forEach { categoryRequest ->
            if (categoryRequest.id > 0) {
                val category = template.categories.first { it.id == categoryRequest.id }
                category.title = categoryRequest.title
                category.shortcut = categoryRequest.shortcut
                category.description = categoryRequest.description
                category.faceToFace = categoryRequest.faceToFace
            } else {
                template.categories.add(
                    Category(
                        title = categoryRequest.title,
                        shortcut = categoryRequest.shortcut,
                        description = categoryRequest.description,
                        faceToFace = categoryRequest.faceToFace,
                        categoryTemplate = template
                    )
                )
            }
        }

        return CategoryTemplateUpdateResult.Success(
            CategoryTemplateWithCategoriesResponse.from(categoryTemplateRepository.save(template))
        )
    }

    @Transactional
    fun delete(id: Long): CategoryTemplateDeleteResult {
        val entity = categoryTemplateRepository.findByIdOrNull(id)
            ?: return CategoryTemplateDeleteResult.NotFound
        val response = CategoryTemplateWithCategoriesResponse.from(entity)
        categoryTemplateRepository.deleteById(id)
        return CategoryTemplateDeleteResult.Success(response)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<CategoryTemplateWithCategoriesResponse> {
        return categoryTemplateRepository.findAll()
            .map { CategoryTemplateWithCategoriesResponse.from(it) }
            .sortedBy { it.title }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): CategoryTemplateWithCategoriesResponse? {
        return categoryTemplateRepository.findByIdOrNull(id)
            ?.let { CategoryTemplateWithCategoriesResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): CategoryTemplate? {
        return categoryTemplateRepository.findByIdOrNull(id)
    }
}
