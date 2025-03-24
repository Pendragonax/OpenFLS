package de.vinz.openfls.domains.categories.dtos

import de.vinz.openfls.domains.categories.entities.CategoryTemplate

data class CategoryTemplateDto(
        var id: Long = 0,
        var title: String = "",
        var description: String = "",
        var withoutClient: Boolean = false,
        var categories: List<CategoryDto> = emptyList()
) {
    companion object {
        fun from(categoryTemplate: CategoryTemplate): CategoryTemplateDto {
            return CategoryTemplateDto(
                    id = categoryTemplate.id,
                    title = categoryTemplate.title,
                    description = categoryTemplate.description,
                    withoutClient = categoryTemplate.withoutClient,
                    categories = categoryTemplate.categories.map { CategoryDto.from(it) }.sortedBy { it.title }
            )
        }
    }
}