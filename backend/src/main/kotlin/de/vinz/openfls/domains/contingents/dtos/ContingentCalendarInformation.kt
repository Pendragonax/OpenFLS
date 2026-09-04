package de.vinz.openfls.domains.contingents.dtos

data class ContingentCalendarInformation(
    val employeeId: Long,
    val days: List<ContingentCalendarDayInformation>,
    val today: ContingentCalendarInformationDto,
    val thisWeek: ContingentCalendarInformationDto,
    val thisMonth: ContingentCalendarInformationDto
)
