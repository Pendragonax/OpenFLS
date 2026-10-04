package de.vinz.openfls.domains.categories.dto

import jakarta.validation.constraints.NotEmpty

data class CategoryUpdateRequest(
    val id: Long = 0,
    @field:NotEmpty
    val title: String = "",
    @field:NotEmpty
    val shortcut: String = "",
    val description: String = "",
    val faceToFace: Boolean = true
)
