package de.vinz.openfls.domains.hourReports.projection

interface HourReportMonthlySummaryGoalHourProjection {
    val id: Long
    val weeklyMinutes: Int
    val hourType: HourReportMonthlySummaryHourTypeProjection
}
