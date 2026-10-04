package de.vinz.openfls.domains.hourReports.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import java.time.LocalDate

data class HourReportMonthlySummaryRowResponse(
        val assistancePlanId: Long,
        val start: LocalDate,
        val end: LocalDate,
        val clientFirstName: String,
        val clientLastName: String,
        val hourMode: AssistancePlanHourMode,
        val year: Int,
        val month: Int,
        val approvedHours: Double,
        val executedHours: Double,
        val executedPercent: Double,
        val missingHours: Double)
