package de.vinz.openfls.domains.hourReports.projection

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode

import java.time.LocalDate

interface HourReportMonthlySummaryProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val client: HourReportMonthlySummaryClientProjection
    val sponsor: HourReportMonthlySummarySponsorProjection
    val institution: HourReportMonthlySummaryInstitutionProjection
    val hourMode: AssistancePlanHourMode
    val hourCorridor: HourReportMonthlySummaryHourCorridorProjection?
    val hours: List<HourReportMonthlySummaryHourProjection>
    val goals: List<HourReportMonthlySummaryGoalProjection>
}
