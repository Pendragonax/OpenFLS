package de.vinz.openfls.domains.categories.dtos

import jakarta.validation.Valid

data class CategoryTemplateCreateRequest(
    val title: String = "",
    val description: String = "",
    val withoutClient: Boolean = false,
    @field:Valid
    val categories: List<CategoryCreateRequest> = emptyList()
)
