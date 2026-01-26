package de.vinz.openfls.domains.services.dtos

data class ServiceCalendarInformation(
    val employeeId: Long,
    val days: List<ServiceCalendarDayInformation>,
    val today: CalendarContingentInformationDTO,
    val lastWeek: CalendarContingentInformationDTO,
    val lastMonth: CalendarContingentInformationDTO
)
