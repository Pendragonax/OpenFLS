package de.vinz.openfls.domains.assistancePlans.dto

sealed class AssistancePlanPreviewListResult {
    data class Success(val previews: List<AssistancePlanPreviewResponse>) : AssistancePlanPreviewListResult()
    data object Forbidden : AssistancePlanPreviewListResult()
}
