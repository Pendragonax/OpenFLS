package de.vinz.openfls.domains.categories.dtos

import de.vinz.openfls.domains.categories.entities.Category
import jakarta.validation.constraints.NotEmpty

data class CategoryDto (
    var id: Long = 0,
    @field:NotEmpty
    var title: String = "",
    @field:NotEmpty
    var shortcut: String = "",
    var description: String = "",
    var faceToFace: Boolean = true,
    var categoryTemplateId: Long = 0
) {
    companion object {
        fun from(category: Category): CategoryDto {
            return CategoryDto(
                    id = category.id,
                    title = category.title,
                    shortcut = category.shortcut,
                    description = category.description,
                    faceToFace = category.faceToFace,
                    categoryTemplateId = category.categoryTemplate?.id ?: 0
            )
        }
    }
}