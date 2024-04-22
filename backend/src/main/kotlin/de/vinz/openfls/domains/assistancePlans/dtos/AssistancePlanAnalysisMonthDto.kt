package de.vinz.openfls.domains.assistancePlans.dtos

import java.time.LocalDate

data class AssistancePlanAnalysisMonthDto(
        val assistancePlanId: Long,
        val start: LocalDate,
        val end: LocalDate,
        val clientFirstName: String,
        val clientLastName: String,
        val year: Int,
        val month: Int,
        val approvedHours: Double,
        val executedHours: Double,
        val executedPercent: Double,
        val missingHours: Double)