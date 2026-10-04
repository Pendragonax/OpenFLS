package de.vinz.openfls.domains.assistancePlans.dto

sealed class AssistancePlanCreateResult {
    data class Success(val response: AssistancePlanResponse) : AssistancePlanCreateResult()
    data class InvalidHours(val reason: String) : AssistancePlanCreateResult()
    data object HourCorridorNotFound : AssistancePlanCreateResult()
    data object ClientNotFound : AssistancePlanCreateResult()
    data object ClientArchived : AssistancePlanCreateResult()
    data object InstitutionNotFound : AssistancePlanCreateResult()
    data object SponsorNotFound : AssistancePlanCreateResult()
    data object HourTypeNotFound : AssistancePlanCreateResult()
}
