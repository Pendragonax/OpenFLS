package de.vinz.openfls.domains.categories.dto

sealed class CategoryTemplateDeleteResult {
    data class Success(val response: CategoryTemplateWithCategoriesResponse) : CategoryTemplateDeleteResult()
    data object NotFound : CategoryTemplateDeleteResult()
}
