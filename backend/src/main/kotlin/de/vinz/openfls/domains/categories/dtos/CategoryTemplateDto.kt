package de.vinz.openfls.domains.categories.dtos

import de.vinz.openfls.domains.categories.entities.CategoryTemplate

/**
 * Schlanke Standard-Variante ohne die Kategorien-Relation. Für den Anwendungsfall
 * „Kategorien mitlesen" siehe [CategoryTemplateWithCategories].
 */
data class CategoryTemplateDto(
        var id: Long = 0,
        var title: String = "",
        var description: String = "",
        var withoutClient: Boolean = false
) {
    companion object {
        fun from(categoryTemplate: CategoryTemplate): CategoryTemplateDto {
            return CategoryTemplateDto(
                    id = categoryTemplate.id,
                    title = categoryTemplate.title,
                    description = categoryTemplate.description,
                    withoutClient = categoryTemplate.withoutClient
            )
        }
    }
}
