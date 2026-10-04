package de.vinz.openfls.domains.hourReports.dto

sealed class HourReportResult {
    data class Success(val rows: List<HourReportRowResponse>) : HourReportResult()
    data object Forbidden : HourReportResult()
    data object InvalidTimeRange : HourReportResult()
}
