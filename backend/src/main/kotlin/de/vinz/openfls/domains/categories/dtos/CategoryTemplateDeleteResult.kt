package de.vinz.openfls.domains.categories.dtos

sealed class CategoryTemplateDeleteResult {
    data class Success(val response: CategoryTemplateWithCategoriesResponse) : CategoryTemplateDeleteResult()
    data object NotFound : CategoryTemplateDeleteResult()
}
