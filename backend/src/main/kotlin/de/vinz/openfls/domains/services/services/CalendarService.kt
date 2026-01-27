package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.absence.AbsenceService
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
    private val contingentService: ContingentService,
    private val absenceService: AbsenceService
) {

    private val warningPercent = 95.0

    fun getServiceCalendarInformation(employeeId: Long, end: LocalDate): ServiceCalendarInformation {
        val start = end.minusYears(1)
        val contingents = contingentService.getByEmployeeId(employeeId)
        val absenceDates = absenceService.getAllByEmployeeId(employeeId).absenceDates.toMutableList()
        val calendarDays = serviceRepository.findServiceCalendarProjection(employeeId, start, end)
            .groupBy { it.start.toLocalDate() }
            .map {
                val minutes = it.value.sumOf { service -> service.minutes }
                val contingentMinutes = ceil(getContingentMinutesForWorkday(it.key, contingents)).toInt()
                val absentFound = absenceDates.contains(it.key)
                if (absentFound) {
                    absenceDates.remove(it.key)
                }
                generate(it.key, minutes, contingentMinutes, absentFound)
            }
        val absenceDays = absenceDates.map { date ->
            generate(date, 0, 0, true)
        }

        val todayInformation = getTodayContingentInformation(calendarDays, contingents, absenceDates)
        val lastWeekInformation = getLastWeekContingentInformation(end, calendarDays, contingents, absenceDates)
        val lastMonthInformation = getLastMonthContingentInformation(end, calendarDays, contingents, absenceDates)


        val allDays = (calendarDays + absenceDays).sortedBy { it.date }
        return ServiceCalendarInformation(employeeId, allDays, todayInformation, lastWeekInformation, lastMonthInformation)
    }

    private fun getTodayContingentInformation(calendarDays: List<ServiceCalendarDayInformation>, contingents: List<ContingentDto>, absenceDates: List<LocalDate>): CalendarContingentInformationDTO {
        val contingentMinutes = ceil(getContingentMinutesForWorkday(LocalDate.now(), contingents)).toInt()

        if (absenceDates.contains(LocalDate.now())) {
            return generateContingentInformationDTO(0, 0)
        }

        val todayCalendarDay = calendarDays.firstOrNull { it.date.isEqual(LocalDate.now()) }
        val executedMinutes = todayCalendarDay?.let { it.executedHours * 60 + it.executedMinutes } ?: 0

        return generateContingentInformationDTO(executedMinutes, contingentMinutes)
    }

    private fun getLastWeekContingentInformation(end: LocalDate, calendarDays: List<ServiceCalendarDayInformation>, contingents: List<ContingentDto>, absenceDates: List<LocalDate>): CalendarContingentInformationDTO {
        val lastWeekStart = end.minusDays(6)

        return getStartToEndContingentInformation(lastWeekStart, end, calendarDays, contingents, absenceDates)
    }

    private fun getLastMonthContingentInformation(end: LocalDate, calendarDays: List<ServiceCalendarDayInformation>, contingents: List<ContingentDto>, absenceDates: List<LocalDate>): CalendarContingentInformationDTO {
        val lastMonthStart = end.minusMonths(1).plusDays(1)

        return getStartToEndContingentInformation(lastMonthStart, end, calendarDays, contingents, absenceDates)
    }

    private fun getStartToEndContingentInformation(start: LocalDate, end: LocalDate, calendarDays: List<ServiceCalendarDayInformation>, contingents: List<ContingentDto>, absenceDates: List<LocalDate>): CalendarContingentInformationDTO {
        val contingentMinutes = getContingentMinutesFor(start, end, contingents)
        val executedMinutes = getExecutedMinutesFor(start, end, calendarDays)
        val absenceMinutes = absenceDates
            .filter { !it.isBefore(start) && !it.isAfter(end) }
            .sumOf { getContingentMinutesForWorkday(it, contingents).toInt() }

        return generateContingentInformationDTO(executedMinutes, contingentMinutes - absenceMinutes)
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

    private fun generate(date: LocalDate, executedMinutes: Int, contingentMinutes: Int, absence: Boolean): ServiceCalendarDayInformation {
        val differenceMinutes = executedMinutes - contingentMinutes
        val executedPercentage = if (contingentMinutes == 0) {
            1.0
        } else {
            executedMinutes.toDouble() / contingentMinutes.toDouble()
        }

        return ServiceCalendarDayInformation(
            date = date,
            absence = absence,
            executedPercentage = round(executedPercentage * 10000) / 100,
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
            executedPercentage = round(executedPercentage * 10000) / 100,
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