package de.vinz.openfls.domains.categories.dtos

data class CategoryTemplateCreateDto(
        var title: String = "",
        var description: String = "",
        var withoutClient: Boolean = false,
        var categories: List<CategoryDto> = emptyList()
)
