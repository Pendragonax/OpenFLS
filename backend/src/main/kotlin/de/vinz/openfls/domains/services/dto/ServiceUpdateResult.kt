package de.vinz.openfls.domains.services.dto

sealed class ServiceUpdateResult {
    data class Success(val response: ServiceWithGoalsAndCategoriesResponse) : ServiceUpdateResult()
    data object NotFound : ServiceUpdateResult()
    data object Forbidden : ServiceUpdateResult()
    data object ClientNotFound : ServiceUpdateResult()
    data object AssistancePlanNotFound : ServiceUpdateResult()
    data object HourTypeNotFound : ServiceUpdateResult()
    data object InstitutionNotFound : ServiceUpdateResult()
    data object GoalNotFound : ServiceUpdateResult()
    data object CategoryNotFound : ServiceUpdateResult()
    data object ClientArchived : ServiceUpdateResult()
    data object InvalidTimeRange : ServiceUpdateResult()
}
