package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.absence.Absence
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.math.ceil

@Service
class ContingentCalculationService {

    @InternalEntityApi
    fun calculateContingentHoursBy(
        year: Int,
        contingent: Contingent,
        absences: List<Absence>
    ): List<Double> {
        val workdayDailyHours = contingent.weeklyServiceHours / 5
        val workdays = DateService.calculateWorkdaysInHesseBetween(contingent.start, contingent.end, year)
        val absenceDays = countAbsenceDaysInContingentForYear(year, contingent, absences)
        val realWorkDays = workdays - absenceDays

        val monthlyHours = ArrayList<Double>()
        monthlyHours.add(0.0)

        for (month in 1..12) {
            monthlyHours.add(calculateContingentHoursBy(year, month, contingent, absences))
        }

        monthlyHours[0] = TimeDoubleService.convertDoubleToTimeDouble(realWorkDays * workdayDailyHours)

        return monthlyHours
    }

    @InternalEntityApi
    fun calculateContingentHoursBy(
        year: Int,
        month: Int,
        contingent: Contingent,
        absences: List<Absence>
    ): Double {
        if (!isContingentInYearMonth(year, month, contingent)) {
            return 0.0
        }

        // end date or the last day of the year when there is no end set
        val end = contingent.end ?: LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
        val workdays = DateService.countWorkDaysOfMonthAndYearBetweenStartAndEnd(year, month, contingent.start, end)
        val absenceDays = countAbsenceDaysBy(year, month, contingent, absences)
        return TimeDoubleService.convertDoubleToTimeDouble((workdays - absenceDays) * (contingent.weeklyServiceHours / 5))
    }

    @InternalEntityApi
    fun countAbsenceDaysBy(
        year: Int,
        month: Int,
        contingent: Contingent,
        absences: List<Absence>
    ): Int {
        val employeeAbsences = absences
            .filter { it.employeeId == contingent.employee?.id }
            .map { it.absenceDate }

        val absenceDaysInMonth = employeeAbsences.count { isAbsenceIn(year, month, contingent, it) }

        return absenceDaysInMonth
    }

    @InternalEntityApi
    fun countAbsenceDaysInContingentForYear(
        year: Int,
        contingent: Contingent,
        absences: List<Absence>
    ): Int {
        val employeeAbsences = absences
            .filter { it.employeeId == contingent.employee?.id }
            .map { it.absenceDate }

        val absenceDaysInMonth = employeeAbsences
            .count { isAbsenceIn(year, contingent, it) }

        return absenceDaysInMonth
    }

    @InternalEntityApi
    fun isContingentInYearMonth(year: Int, month: Int, contingent: Contingent): Boolean {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return (contingent.start <= end) &&
                ((contingent.end?.let { it >= start } ?: true))
    }

    @InternalEntityApi
    fun calculateContingentMinutesForWorkdayBy(
        date: LocalDate,
        contingents: List<Contingent>
    ): Double {
        if (!DateService.isWorkday(date)) {
            return 0.0
        }

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

    @InternalEntityApi
    fun calculateContingentMinutesFor(
        start: LocalDate,
        end: LocalDate,
        contingents: List<Contingent>
    ): Int {
        var totalContingentMinutes = 0.0
        var currentDate = start

        while (!currentDate.isAfter(end)) {
            totalContingentMinutes += calculateContingentMinutesForWorkdayBy(currentDate, contingents)
            currentDate = currentDate.plusDays(1)
        }

        return ceil(totalContingentMinutes).toInt()
    }

    private fun isAbsenceIn(
        year: Int,
        contingent: Contingent,
        absence: LocalDate
    ): Boolean {
        return absence.year == year && absence >= contingent.start && (contingent.end?.let { absence <= it } ?: true)
    }

    private fun isAbsenceIn(
        year: Int,
        month: Int,
        contingent: Contingent,
        absence: LocalDate
    ): Boolean {
        return absence.year == year && absence.monthValue == month && absence >= contingent.start &&
                (contingent.end?.let { absence <= it } ?: true)
    }
}
