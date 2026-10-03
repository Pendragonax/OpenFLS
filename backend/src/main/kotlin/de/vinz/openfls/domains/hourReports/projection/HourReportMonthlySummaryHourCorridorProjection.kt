package de.vinz.openfls.domains.hourReports.projection

interface HourReportMonthlySummaryHourCorridorProjection {
    val id: Long
    val title: String
    val weeklyMinutesFrom: Int
    val weeklyMinutesTill: Int
    val hourType: HourReportMonthlySummaryHourTypeProjection?
}
