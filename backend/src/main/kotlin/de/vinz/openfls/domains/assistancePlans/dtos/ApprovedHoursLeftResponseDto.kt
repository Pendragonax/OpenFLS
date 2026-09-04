package de.vinz.openfls.domains.assistancePlans.dtos

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode

data class ApprovedHoursLeftResponseDto(
    val assistancePlanId: Long,
    val hourMode: AssistancePlanHourMode,
    val approvedHoursFrom: Double,
    val approvedHoursTo: Double,
    val hourTypeEvaluation: List<HourTypeEvaluationDto>
) {
    data class HourTypeEvaluationDto(
        val hourTypeName: String,
        val leftThisWeek: Double,
        val leftThisMonth: Double,
        val leftThisYear: Double,
        val leftComplete: Double
    )
}
