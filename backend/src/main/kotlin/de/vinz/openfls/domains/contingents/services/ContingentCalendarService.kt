package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.absence.AbsenceService
import de.vinz.openfls.domains.contingents.dtos.ContingentCalendarInformationDTO
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.contingents.dtos.ContingentCalendarDayInformation
import de.vinz.openfls.domains.contingents.dtos.ContingentCalendarInformation
import de.vinz.openfls.domains.services.ServiceRepository
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
    private val serviceRepository: ServiceRepository,
    private val contingentService: ContingentService,
    private val absenceService: AbsenceService
) {

    private val warningPercent = 95.0

    fun generateContingentCalendarInformationFor(employeeId: Long, end: LocalDate): ContingentCalendarInformation {
        val start = end.minusYears(1)
        val contingents = contingentService.getByEmployeeId(employeeId)
        val absenceDates = absenceService.getAllByEmployeeId(employeeId).absenceDates.toMutableList()
        val calendarDayInformations =
            generateContingentCalendarDayInformationFor(employeeId, start, end, contingents, absenceDates)
        val absenceDays = absenceDates.map { date ->
            generate(date, 0, 0, true)
        }

        val todayInformation = generateForToday(calendarDayInformations, contingents, absenceDates)
        val lastWeekInformation =
            generateForThisWeek(end, calendarDayInformations, contingents, absenceDates)
        val lastMonthInformation =
            generateForThisMonth(end, calendarDayInformations, contingents, absenceDates)

        val allDays = (calendarDayInformations + absenceDays).sortedBy { it.date }
        return ContingentCalendarInformation(
            employeeId,
            allDays,
            todayInformation,
            lastWeekInformation,
            lastMonthInformation
        )
    }

    private fun generateForToday(
        calendarDayInformations: List<ContingentCalendarDayInformation>,
        contingents: List<ContingentDto>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarInformationDTO {
        val contingentMinutes =
            ceil(contingentService.getContingentMinutesForWorkday(LocalDate.now(), contingents)).toInt()

        if (absenceDates.contains(LocalDate.now())) {
            return generateContingentInformationDTO(0, 0)
        }

        val todayCalendarDay = calendarDayInformations.firstOrNull { it.date.isEqual(LocalDate.now()) }
        val executedMinutes = todayCalendarDay?.let { it.executedHours * 60 + it.executedMinutes } ?: 0

        return generateContingentInformationDTO(executedMinutes, contingentMinutes)
    }

    private fun generateForThisWeek(
        end: LocalDate,
        calendarDayInformations: List<ContingentCalendarDayInformation>,
        contingents: List<ContingentDto>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarInformationDTO {
        val thisWeekStart = end.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        return generateFor(thisWeekStart, end, calendarDayInformations, contingents, absenceDates)
    }

    private fun generateForThisMonth(
        end: LocalDate,
        calendarDayInformations: List<ContingentCalendarDayInformation>,
        contingents: List<ContingentDto>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarInformationDTO {
        val thisMonthStart = end.withDayOfMonth(1)

        return generateFor(thisMonthStart, end, calendarDayInformations, contingents, absenceDates)
    }

    private fun generateFor(
        start: LocalDate,
        end: LocalDate,
        calendarDayInformations: List<ContingentCalendarDayInformation>,
        contingents: List<ContingentDto>,
        absenceDates: List<LocalDate>
    ): ContingentCalendarInformationDTO {
        val contingentMinutes = contingentService.getContingentMinutesFor(start, end, contingents)
        val executedMinutes = sumExecutedMinutesFor(start, end, calendarDayInformations)
        val absenceMinutes = absenceDates
            .filter { !it.isBefore(start) && !it.isAfter(end) }
            .sumOf { contingentService.getContingentMinutesForWorkday(it, contingents).toInt() }

        return generateContingentInformationDTO(executedMinutes, contingentMinutes - absenceMinutes)
    }

    private fun generateContingentCalendarDayInformationFor(
        employeeId: Long,
        start: LocalDate,
        end: LocalDate,
        contingents: List<ContingentDto>,
        absenceDates: MutableList<LocalDate>
    ): List<ContingentCalendarDayInformation> {
        return serviceRepository.findServiceCalendarProjection(employeeId, start, end)
            .groupBy { it.start.toLocalDate() }
            .map {
                val minutes = it.value.sumOf { service -> service.minutes }
                val contingentMinutes =
                    ceil(contingentService.getContingentMinutesForWorkday(it.key, contingents)).toInt()
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
        calendarDayInformations: List<ContingentCalendarDayInformation>
    ): Int {
        return calendarDayInformations.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
            .sumOf { it.executedHours * 60 + it.executedMinutes }
    }

    private fun generate(
        date: LocalDate,
        executedMinutes: Int,
        contingentMinutes: Int,
        absent: Boolean
    ): ContingentCalendarDayInformation {
        val differenceMinutes = executedMinutes - contingentMinutes
        val executedPercentage = if (contingentMinutes == 0) {
            1.0
        } else {
            executedMinutes.toDouble() / contingentMinutes.toDouble()
        }

        return ContingentCalendarDayInformation(
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

    private fun generateContingentInformationDTO(
        executedMinutes: Int,
        contingentMinutes: Int
    ): ContingentCalendarInformationDTO {
        val differenceMinutes = executedMinutes - contingentMinutes
        val executedPercentage = if (contingentMinutes == 0) {
            1.0
        } else {
            executedMinutes.toDouble() / contingentMinutes.toDouble()
        }

        return ContingentCalendarInformationDTO(
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