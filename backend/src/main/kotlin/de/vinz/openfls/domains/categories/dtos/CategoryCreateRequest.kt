package de.vinz.openfls.domains.categories.dtos

import jakarta.validation.constraints.NotEmpty

data class CategoryCreateRequest(
    @field:NotEmpty
    val title: String = "",
    @field:NotEmpty
    val shortcut: String = "",
    val description: String = "",
    val faceToFace: Boolean = true
)
