package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode

data class ApprovedHoursLeftResponse(
    val assistancePlanId: Long,
    val hourMode: AssistancePlanHourMode,
    val approvedHoursFrom: Double,
    val approvedHoursTo: Double,
    val hourTypeEvaluation: List<ApprovedHoursLeftHourTypeResponse>
)
