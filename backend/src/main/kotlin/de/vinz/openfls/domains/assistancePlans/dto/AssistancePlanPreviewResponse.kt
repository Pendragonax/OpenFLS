package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import java.time.LocalDate

data class AssistancePlanPreviewResponse(
    val id: Long,
    val clientId: Long,
    val institutionId: Long,
    val sponsorId: Long,
    val clientFirstname: String,
    val clientLastname: String,
    val clientArchived: Boolean,
    val institutionName: String,
    val sponsorName: String,
    val start: LocalDate,
    val end: LocalDate,
    val isActive: Boolean,
    val isFavorite: Boolean,
    val hasIllegalHours: Boolean,
    val hourMode: AssistancePlanHourMode,
    val approvedHoursFrom: Double,
    val approvedHoursTo: Double,
    val approvedHoursPerWeek: Double,
    val approvedHoursThisYearFrom: Double,
    val approvedHoursThisYearTill: Double,
    val approvedHoursThisYear: Double,
    val executedHoursThisYear: Double,
    val approvedHoursLeftThisYear: Double,
    val approvedHoursThisAssistancePlanFrom: Double,
    val approvedHoursThisAssistancePlanTill: Double,
    val approvedHoursThisAssistancePlan: Double,
    val executedHoursThisAssistancePlan: Double,
    val approvedHoursLeftThisAssistancePlan: Double
)
