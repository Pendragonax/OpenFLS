package de.vinz.openfls.domains.assistancePlans.dto

data class ApprovedHoursLeftHourTypeResponse(
    val hourTypeName: String,
    val leftThisWeek: Double,
    val leftThisMonth: Double,
    val leftThisYear: Double,
    val leftComplete: Double
)
