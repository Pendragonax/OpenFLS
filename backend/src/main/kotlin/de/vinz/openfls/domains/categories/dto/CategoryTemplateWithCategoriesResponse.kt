package de.vinz.openfls.domains.categories.dto

import de.vinz.openfls.domains.categories.entity.CategoryTemplate

data class CategoryTemplateWithCategoriesResponse(
    var id: Long = 0,
    var title: String = "",
    var description: String = "",
    var withoutClient: Boolean = false,
    var categories: List<CategoryResponse> = emptyList()
) {
    companion object {
        fun from(categoryTemplate: CategoryTemplate): CategoryTemplateWithCategoriesResponse {
            return CategoryTemplateWithCategoriesResponse(
                id = categoryTemplate.id,
                title = categoryTemplate.title,
                description = categoryTemplate.description,
                withoutClient = categoryTemplate.withoutClient,
                categories = categoryTemplate.categories.map { CategoryResponse.from(it) }.sortedBy { it.title }
            )
        }
    }
}
