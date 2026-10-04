package de.vinz.openfls.domains.hourReports.projection

interface HourReportMonthlySummaryGoalProjection {
    val id: Long
    val title: String
    val description: String
    val hours: List<HourReportMonthlySummaryGoalHourProjection>
}
