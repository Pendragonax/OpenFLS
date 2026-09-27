package de.vinz.openfls.domains.categories.dto

import jakarta.validation.constraints.NotEmpty

data class CategoryCreateRequest(
    @field:NotEmpty
    val title: String = "",
    @field:NotEmpty
    val shortcut: String = "",
    val description: String = "",
    val faceToFace: Boolean = true
)
