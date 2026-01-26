package de.vinz.openfls.domains.services.dtos

import java.time.LocalDate

data class ServiceCalendarDayInformation(
    var date: LocalDate,
    var serviceCount: Int,
    var executedHours: Int,
    var executedMinutes: Int,
    var contingentHours: Int,
    var contingentMinutes: Int,
    var differenceHours: Int,
    var differenceMinutes: Int
)