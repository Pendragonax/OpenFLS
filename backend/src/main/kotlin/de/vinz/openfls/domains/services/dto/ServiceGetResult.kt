package de.vinz.openfls.domains.services.dto

sealed class ServiceGetResult {
    data class Success(val response: ServiceWithGoalsAndCategoriesResponse) : ServiceGetResult()
    data object NotFound : ServiceGetResult()
    data object Forbidden : ServiceGetResult()
}
