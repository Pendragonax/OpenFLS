package de.vinz.openfls.domains.categories.dtos

import de.vinz.openfls.domains.categories.entities.Category

data class CategoryResponse(
    var id: Long = 0,
    var title: String = "",
    var shortcut: String = "",
    var description: String = "",
    var faceToFace: Boolean = true,
    var categoryTemplateId: Long = 0
) {
    companion object {
        fun from(category: Category): CategoryResponse {
            return CategoryResponse(
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
