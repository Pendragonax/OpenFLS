package de.vinz.openfls.domains.hourReports.dto

data class HourReportMonthlySummaryResponse(
        val year: Int,
        val month: Int,
        val approvedHours: Double,
        val executedHours: Double,
        val executedPercent: Double,
        val missingHours: Double,
        val rows: List<HourReportMonthlySummaryRowResponse>)