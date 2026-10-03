package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.categories.entity.Category

data class ClientArchiveExportCategoryDto(
    var id: Long = 0,
    var title: String = "",
    var shortcut: String = ""
) {
    companion object {
        fun from(category: Category): ClientArchiveExportCategoryDto {
            return ClientArchiveExportCategoryDto(
                id = category.id,
                title = category.title,
                shortcut = category.shortcut
            )
        }
    }
}
