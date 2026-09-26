package de.vinz.openfls.domains.categories.dtos

data class CategoryTemplateUpdateDto(
        var id: Long = 0,
        var title: String = "",
        var description: String = "",
        var withoutClient: Boolean = false,
        var categories: List<CategoryDto> = emptyList()
)
