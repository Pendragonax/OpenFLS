package de.vinz.openfls.domains.contingents.service

import de.vinz.openfls.domains.absence.service.AbsenceService
import de.vinz.openfls.domains.contingents.dto.ContingentCalendarPeriodResponse
import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.contingents.dto.ContingentCalendarDayResponse
import de.vinz.openfls.domains.contingents.dto.ContingentCalendarResponse
import de.vinz.openfls.domains.services.service.ServiceService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil
import kotlin.math.round

@Service
@Transactional(readOnly = true)
class ContingentCalendarService(
    private val serviceService: ServiceService,
    private val contingentService: ContingentService,
    private val contingentCalculationService: ContingentCalculationService,
    private val absenceService: AbsenceService
) {

    private val warningPercent = 95.0

    fun generateContingentCalendarFor(employeeId: Long, end: LocalDate): ContingentCalendarResponse {
        val start = end.minusYears(1)
        val contingents = contingentService.getAllEntitiesByEmployeeId(employeeId)
        val absenceDates = absenceService.getAllEntitiesByEmployeeId(employeeId).map { it.absenceDate }.toMutableList()
        val calendarDayInformations =
            generateContingentCalendarDaysFor(employeeId, start, end, contingents, absenceDates)
        val absenceDays = absenceDates.map { date ->
            generate(date, 0, 0, true)
        }

        val todayInformation = generateForToday(calendarDayInformations, contingents, absenceDates)
        val lastWeekInformation =
            generateForThisWeek(end, calendarDayInformations, contingents, absenceDates)
        val lastMonthInformation =
            generateForThisMonth(end, calendarDayInformations, contingents, absenceDates)

        val allDays = (calendarDayInformations + absenceDays).sortedBy { it.date }
        return ContingentCalendarResponse(
            employeeId,
            allDays,
            todayInformation,
            lastWeekInformation,
            lastMonthInformation
        )
    }

    private fun generateForToday(
        calendarDayInformations: List<ContingentCalendarDayResponse>,
        contingents: List<Contingent>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarPeriodResponse {
        val contingentMinutes =
            ceil(contingentCalculationService.calculateContingentMinutesForWorkdayBy(LocalDate.now(), contingents)).toInt()

        if (absenceDates.contains(LocalDate.now())) {
            return generateContingentPeriodResponse(0, 0)
        }

        val todayCalendarDay = calendarDayInformations.firstOrNull { it.date.isEqual(LocalDate.now()) }
        val executedMinutes = todayCalendarDay?.let { it.executedHours * 60 + it.executedMinutes } ?: 0

        return generateContingentPeriodResponse(executedMinutes, contingentMinutes)
    }

    private fun generateForThisWeek(
        end: LocalDate,
        calendarDayInformations: List<ContingentCalendarDayResponse>,
        contingents: List<Contingent>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarPeriodResponse {
        val thisWeekStart = end.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        return generateFor(thisWeekStart, end, calendarDayInformations, contingents, absenceDates)
    }

    private fun generateForThisMonth(
        end: LocalDate,
        calendarDayInformations: List<ContingentCalendarDayResponse>,
        contingents: List<Contingent>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarPeriodResponse {
        val thisMonthStart = end.withDayOfMonth(1)

        return generateFor(thisMonthStart, end, calendarDayInformations, contingents, absenceDates)
    }

    private fun generateFor(
        start: LocalDate,
        end: LocalDate,
        calendarDayInformations: List<ContingentCalendarDayResponse>,
        contingents: List<Contingent>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarPeriodResponse {
        val contingentMinutes = contingentCalculationService.calculateContingentMinutesFor(start, end, contingents)
        val executedMinutes = sumExecutedMinutesFor(start, end, calendarDayInformations)
        val absenceMinutes = absenceDates
            .filter { !it.isBefore(start) && !it.isAfter(end) }
            .sumOf { contingentCalculationService.calculateContingentMinutesForWorkdayBy(it, contingents).toInt() }

        return generateContingentPeriodResponse(executedMinutes, contingentMinutes - absenceMinutes)
    }

    private fun generateContingentCalendarDaysFor(
        employeeId: Long,
        start: LocalDate,
        end: LocalDate,
        contingents: List<Contingent>,
        absenceDates: MutableList<LocalDate>
    ): List<ContingentCalendarDayResponse> {
        return serviceService.getCalendarServicesByEmployeeIdAndStartAndEnd(employeeId, start, end)
            .groupBy { it.start.toLocalDate() }
            .map {
                val minutes = it.value.sumOf { service -> service.minutes }
                val contingentMinutes =
                    ceil(contingentCalculationService.calculateContingentMinutesForWorkdayBy(it.key, contingents)).toInt()
                val absentFound = absenceDates.contains(it.key)
                if (absentFound) {
                    absenceDates.remove(it.key)
                }
                generate(it.key, minutes, contingentMinutes, absentFound)
            }
    }

    private fun sumExecutedMinutesFor(
        start: LocalDate,
        end: LocalDate,
        calendarDayInformations: List<ContingentCalendarDayResponse>
    ): Int {
        return calendarDayInformations.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
            .sumOf { it.executedHours * 60 + it.executedMinutes }
    }

    private fun generate(
        date: LocalDate,
        executedMinutes: Int,
        contingentMinutes: Int,
        absent: Boolean
    ): ContingentCalendarDayResponse {
        val differenceMinutes = executedMinutes - contingentMinutes
        val executedPercentage = if (contingentMinutes == 0) {
            1.0
        } else {
            executedMinutes.toDouble() / contingentMinutes.toDouble()
        }

        return ContingentCalendarDayResponse(
            date = date,
            absence = absent,
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

    private fun generateContingentPeriodResponse(
        executedMinutes: Int,
        contingentMinutes: Int
    ): ContingentCalendarPeriodResponse {
        val differenceMinutes = executedMinutes - contingentMinutes
        val executedPercentage = if (contingentMinutes == 0) {
            1.0
        } else {
            executedMinutes.toDouble() / contingentMinutes.toDouble()
        }

        return ContingentCalendarPeriodResponse(
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
