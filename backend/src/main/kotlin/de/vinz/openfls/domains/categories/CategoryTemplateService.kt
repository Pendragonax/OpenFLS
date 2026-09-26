package de.vinz.openfls.domains.categories

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateCreateRequest
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateUpdateRequest
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateWithCategoriesResponse
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryRepository
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
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
    fun update(request: CategoryTemplateUpdateRequest): CategoryTemplateWithCategoriesResponse {
        val template = categoryTemplateRepository.findById(request.id)
            .orElseThrow { IllegalArgumentException("category template with id ${request.id} not found") }

        template.title = request.title
        template.description = request.description
        template.withoutClient = request.withoutClient

        val requestedIds = request.categories.filter { it.id > 0 }.map { it.id }.toSet()
        val categoriesToDelete = template.categories.filter { it.id !in requestedIds }
        template.categories.removeAll(categoriesToDelete.toSet())
        categoryRepository.deleteAll(categoriesToDelete)

        request.categories.forEach { categoryRequest ->
            if (categoryRequest.id > 0) {
                val category = template.categories.firstOrNull { it.id == categoryRequest.id }
                    ?: throw IllegalArgumentException(
                        "category with id ${categoryRequest.id} does not belong to category template ${template.id}"
                    )
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

        return CategoryTemplateWithCategoriesResponse.from(categoryTemplateRepository.save(template))
    }

    @Transactional
    fun delete(id: Long) {
        categoryTemplateRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<CategoryTemplateWithCategoriesResponse> {
        return categoryTemplateRepository.findAll()
            .map { CategoryTemplateWithCategoriesResponse.from(it) }
            .sortedBy { it.title }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): CategoryTemplateWithCategoriesResponse? {
        return categoryTemplateRepository.findById(id).orElse(null)
            ?.let { CategoryTemplateWithCategoriesResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): CategoryTemplate? {
        return categoryTemplateRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return categoryTemplateRepository.existsById(id)
    }
}
