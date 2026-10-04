package de.vinz.openfls.domains.assistancePlans.dto

sealed class AssistancePlanDeleteResult {
    data class Success(val response: AssistancePlanResponse) : AssistancePlanDeleteResult()
    data object NotFound : AssistancePlanDeleteResult()
}
