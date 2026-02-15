package de.vinz.openfls.domains.assistancePlans.dtos

import java.time.LocalDate

data class AssistancePlanPreviewDto(
    val id: Long,
    val clientId: Long,
    val institutionId: Long,
    val sponsorId: Long,
    val clientFirstname: String,
    val clientLastname: String,
    val institutionName: String,
    val sponsorName: String,
    val start: LocalDate,
    val end: LocalDate,
    val isActive: Boolean,
    val isFavorite: Boolean,
    val approvedHoursPerWeek: Double,
    val approvedHoursThisYear: Double,
    val executedHoursThisYear: Double
)
