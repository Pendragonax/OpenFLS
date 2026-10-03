package de.vinz.openfls.domains.hourReports.projection

import java.time.LocalDate

interface HourReportMonthlySummaryAssistancePlanProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
}
