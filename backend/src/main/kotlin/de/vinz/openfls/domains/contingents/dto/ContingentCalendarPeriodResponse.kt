package de.vinz.openfls.domains.contingents.dto

data class ContingentCalendarPeriodResponse(
    val executedPercentage: Double,
    val warningPercent: Double,
    val executedHours: Int,
    val executedMinutes: Int,
    val contingentHours: Int,
    val contingentMinutes: Int,
    val differenceHours: Int,
    val differenceMinutes: Int
)
