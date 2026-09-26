package de.vinz.openfls.domains.contingents.dtos

data class ContingentCalendarResponse(
    val employeeId: Long,
    val days: List<ContingentCalendarDayResponse>,
    val today: ContingentCalendarPeriodResponse,
    val thisWeek: ContingentCalendarPeriodResponse,
    val thisMonth: ContingentCalendarPeriodResponse
)
