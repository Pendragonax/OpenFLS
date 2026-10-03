package de.vinz.openfls.domains.services.dto

sealed class ServiceCreateResult {
    data class Success(val response: ServiceWithGoalsAndCategoriesResponse) : ServiceCreateResult()
    data object Forbidden : ServiceCreateResult()
    data object ClientNotFound : ServiceCreateResult()
    data object AssistancePlanNotFound : ServiceCreateResult()
    data object HourTypeNotFound : ServiceCreateResult()
    data object InstitutionNotFound : ServiceCreateResult()
    data object GoalNotFound : ServiceCreateResult()
    data object CategoryNotFound : ServiceCreateResult()
    data object ClientArchived : ServiceCreateResult()
    data object InvalidTimeRange : ServiceCreateResult()
}
