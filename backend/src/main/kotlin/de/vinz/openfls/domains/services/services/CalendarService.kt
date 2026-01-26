package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.contingents.services.ContingentService
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.domains.services.dtos.CalendarContingentInformationDTO
import de.vinz.openfls.domains.services.dtos.ServiceCalendarDayInformation
import de.vinz.openfls.domains.services.dtos.ServiceCalendarInformation
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.round

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
class CalendarService(
    private val serviceRepository: ServiceRepository,
    private val contingentService: ContingentService
) {

    private val warningPercent = 0.95

    fun getServiceCalendarInformation(employeeId: Long, end: LocalDate): ServiceCalendarInformation {
        val start = end.minusYears(1)
        val contingents = contingentService.getByEmployeeId(employeeId)
        val calendarDays = serviceRepository.findServiceCalendarProjection(employeeId, start, end)
            .groupBy { it.start.toLocalDate() }
            .map {
                val minutes = it.value.sumOf { service -> service.minutes }
                val contingentMinutes = ceil(getContingentMinutesForWorkday(it.key, contingents)).toInt()
                generate(minutes, contingentMinutes)
            }
        val todayInformation = getTodayContingentInformation(end, calendarDays, contingents)
        val lastWeekInformation = getLastWeekContingentInformation(end, calendarDays, contingents)
        val lastMonthInformation = getLastMonthContingentInformation(end, calendarDays, contingents)

        return ServiceCalendarInformation(employeeId, calendarDays, todayInformation, lastWeekInformation, lastMonthInformation)
    }

    private fun getTodayContingentInformation(end: LocalDate, calendarDays: List<ServiceCalendarDayInformation>, contingents: List<ContingentDto>): CalendarContingentInformationDTO {
        val contingentMinutes = ceil(getContingentMinutesForWorkday(end, contingents)).toInt()
        val todayCalendarDay = calendarDays.firstOrNull { it.date.isEqual(end) }
        val executedMinutes = todayCalendarDay?.let { it.executedHours * 60 + it.executedMinutes } ?: 0

        return generateContingentInformationDTO(executedMinutes, contingentMinutes)
    }

    private fun getLastWeekContingentInformation(end: LocalDate, calendarDays: List<ServiceCalendarDayInformation>, contingents: List<ContingentDto>): CalendarContingentInformationDTO {
        val lastWeekStart = end.minusDays(6)
        val contingentMinutes = getContingentMinutesFor(lastWeekStart, end, contingents)
        val todayExecutedMinutes = getExecutedMinutesFor(lastWeekStart, end, calendarDays)

        return generateContingentInformationDTO(todayExecutedMinutes, contingentMinutes)
    }

    private fun getLastMonthContingentInformation(end: LocalDate, calendarDays: List<ServiceCalendarDayInformation>, contingents: List<ContingentDto>): CalendarContingentInformationDTO {
        val lastWeekStart = end.minusMonths(1).plusDays(1)
        val contingentMinutes = getContingentMinutesFor(lastWeekStart, end, contingents)
        val todayExecutedMinutes = getExecutedMinutesFor(lastWeekStart, end, calendarDays)

        return generateContingentInformationDTO(todayExecutedMinutes, contingentMinutes)
    }

    private fun getExecutedMinutesFor(
        start: LocalDate,
        end: LocalDate,
        calendarDays: List<ServiceCalendarDayInformation>
    ): Int {
        return calendarDays.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
            .sumOf { it.executedHours * 60 + it.executedMinutes }
    }

    private fun getContingentMinutesFor(
        start: LocalDate,
        end: LocalDate,
        contingents: List<ContingentDto>
    ): Int {
        var totalContingentMinutes = 0.0
        var currentDate = start

        while (!currentDate.isAfter(end)) {
            totalContingentMinutes += getContingentMinutesForDay(currentDate, contingents)
            currentDate = currentDate.plusDays(1)
        }

        return ceil(totalContingentMinutes).toInt()
    }

    private fun getContingentMinutesForDay(
        date: LocalDate,
        contingents: List<ContingentDto>
    ): Double {
        val contingentWeeklyHours = contingents.firstOrNull { contingent ->
            (contingent.start.isBefore(date) || contingent.start.isEqual(date)) &&
                    (contingent.end == null || contingent.end!!.isAfter(date) || contingent.end!!.isEqual(date))
        }

        return if (contingentWeeklyHours != null) {
            (contingentWeeklyHours.weeklyServiceHours * 60) / 7
        } else {
            0.0
        }
    }

    private fun getContingentMinutesForWorkday(
        date: LocalDate,
        contingents: List<ContingentDto>
    ): Double {
        val contingentWeeklyHours = contingents.firstOrNull { contingent ->
            (contingent.start.isBefore(date) || contingent.start.isEqual(date)) &&
                    (contingent.end == null || contingent.end!!.isAfter(date) || contingent.end!!.isEqual(date))
        }

        return if (contingentWeeklyHours != null) {
            (contingentWeeklyHours.weeklyServiceHours * 60) / 5
        } else {
            0.0
        }
    }

    private fun generate(executedMinutes: Int, contingentMinutes: Int): ServiceCalendarDayInformation {
        val differenceMinutes = executedMinutes - contingentMinutes

        return ServiceCalendarDayInformation(
            date = LocalDate.now(),
            serviceCount = 0,
            executedHours = executedMinutes / 60,
            executedMinutes = executedMinutes % 60,
            contingentHours = contingentMinutes / 60,
            contingentMinutes = contingentMinutes % 60,
            differenceHours = differenceMinutes / 60,
            differenceMinutes = differenceMinutes % 60
        )
    }

    private fun generateContingentInformationDTO(executedMinutes: Int, contingentMinutes: Int): CalendarContingentInformationDTO {
        val differenceMinutes = executedMinutes - contingentMinutes
        val executedPercentage = if (contingentMinutes == 0) {
            1.0
        } else {
            executedMinutes.toDouble() / contingentMinutes.toDouble()
        }

        return CalendarContingentInformationDTO(
            executedPercentage = executedPercentage,
            warningPercent = warningPercent,
            executedHours = executedMinutes / 60,
            executedMinutes = executedMinutes % 60,
            contingentHours = contingentMinutes / 60,
            contingentMinutes = contingentMinutes % 60,
            differenceHours = differenceMinutes / 60,
            differenceMinutes = differenceMinutes % 60
        )
    }
}