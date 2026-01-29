package de.vinz.openfls.domains.contingents.dtos

import java.time.LocalDate

data class ContingentCalendarDayInformation(
    var date: LocalDate,
    var absence: Boolean,
    val executedPercentage: Double,
    var serviceCount: Int,
    var executedHours: Int,
    var executedMinutes: Int,
    var contingentHours: Int,
    var contingentMinutes: Int,
    var differenceHours: Int,
    var differenceMinutes: Int
)