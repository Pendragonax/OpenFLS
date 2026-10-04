package de.vinz.openfls.domains.hourReports.projection

interface HourReportMonthlySummaryClientProjection {
    val id: Long
    val firstName: String
    val lastName: String
    val phoneNumber: String
    val email: String
    val archived: Boolean
}
