package de.vinz.openfls.domains.hourReports.dto

sealed class HourReportMonthlySummaryResult {
    data class Success(val response: HourReportMonthlySummaryResponse) : HourReportMonthlySummaryResult()
    data object Forbidden : HourReportMonthlySummaryResult()
    data object InvalidPeriod : HourReportMonthlySummaryResult()
}
