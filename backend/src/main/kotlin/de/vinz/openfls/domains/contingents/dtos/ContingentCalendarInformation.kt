package de.vinz.openfls.domains.contingents.dtos

data class ContingentCalendarInformation(
    val employeeId: Long,
    val days: List<ContingentCalendarDayInformation>,
    val today: ContingentCalendarInformationDTO,
    val lastWeek: ContingentCalendarInformationDTO,
    val lastMonth: ContingentCalendarInformationDTO
)
