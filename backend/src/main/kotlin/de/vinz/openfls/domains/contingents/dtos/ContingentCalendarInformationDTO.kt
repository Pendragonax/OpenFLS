package de.vinz.openfls.domains.contingents.dtos

data class ContingentCalendarInformationDTO(
    val executedPercentage: Double,
    val warningPercent: Double,
    val executedHours: Int,
    val executedMinutes: Int,
    val contingentHours: Int,
    val contingentMinutes: Int,
    val differenceHours: Int,
    val differenceMinutes: Int
)
