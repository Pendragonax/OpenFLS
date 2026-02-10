package de.vinz.openfls.domains.assistancePlans.dtos

data class ApprovedHoursLeftResponseDTO(
    val assistancePlanId: Long,
    val hourTypeEvaluation: List<HourTypeEvaluationDTO>
) {
    data class HourTypeEvaluationDTO(
        val hourTypeName: String,
        val leftThisWeek: Double,
        val leftThisMonth: Double,
        val leftThisYear: Double
    )
}
