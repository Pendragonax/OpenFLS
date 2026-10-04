package de.vinz.openfls.domains.hourReports.projection

interface HourReportMonthlySummaryHourProjection {
    val id: Long
    val weeklyMinutes: Int
    val hourType: HourReportMonthlySummaryHourTypeProjection
    val assistancePlan: HourReportMonthlySummaryAssistancePlanProjection

}
