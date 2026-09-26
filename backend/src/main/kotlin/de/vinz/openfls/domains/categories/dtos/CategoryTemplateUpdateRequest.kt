package de.vinz.openfls.domains.categories.dtos

import jakarta.validation.Valid

data class CategoryTemplateUpdateRequest(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val withoutClient: Boolean = false,
    @field:Valid
    val categories: List<CategoryUpdateRequest> = emptyList()
)
