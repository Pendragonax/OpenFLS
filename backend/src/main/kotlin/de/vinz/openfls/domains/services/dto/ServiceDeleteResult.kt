package de.vinz.openfls.domains.services.dto

sealed class ServiceDeleteResult {
    data class Success(val response: ServiceWithGoalsAndCategoriesResponse) : ServiceDeleteResult()
    data object NotFound : ServiceDeleteResult()
    data object Forbidden : ServiceDeleteResult()
    data object ClientArchived : ServiceDeleteResult()
}
