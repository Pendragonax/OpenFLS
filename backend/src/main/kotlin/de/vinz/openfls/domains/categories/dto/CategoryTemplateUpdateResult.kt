package de.vinz.openfls.domains.categories.dto

sealed class CategoryTemplateUpdateResult {
    data class Success(val response: CategoryTemplateWithCategoriesResponse) : CategoryTemplateUpdateResult()
    data object NotFound : CategoryTemplateUpdateResult()
    data class CategoryNotInTemplate(val message: String) : CategoryTemplateUpdateResult()
}
