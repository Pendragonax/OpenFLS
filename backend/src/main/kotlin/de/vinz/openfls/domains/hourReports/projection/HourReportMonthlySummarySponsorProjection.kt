package de.vinz.openfls.domains.hourReports.projection

interface HourReportMonthlySummarySponsorProjection {
    val id: Long
    val name: String
    val payOverhang: Boolean
    val payExact: Boolean
}
