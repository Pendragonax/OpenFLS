package de.vinz.openfls.domains.assistancePlans.dto

sealed class AssistancePlanUpdateResult {
    data class Success(val response: AssistancePlanResponse) : AssistancePlanUpdateResult()
    data object NotFound : AssistancePlanUpdateResult()
    data object HourModeChanged : AssistancePlanUpdateResult()
    data class InvalidHours(val reason: String) : AssistancePlanUpdateResult()
    data object HourCorridorNotFound : AssistancePlanUpdateResult()
    data object ClientNotFound : AssistancePlanUpdateResult()
    data object ClientArchived : AssistancePlanUpdateResult()
    data object InstitutionNotFound : AssistancePlanUpdateResult()
    data object SponsorNotFound : AssistancePlanUpdateResult()
    data object HourTypeNotFound : AssistancePlanUpdateResult()
}
