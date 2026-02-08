package de.vinz.openfls.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.YearOutOfRangeException
import java.time.*
import java.time.temporal.ChronoUnit

class DateService {
    companion object {
        fun getStartAndEndInYear(year: Int, start: LocalDate, end: LocalDate): Pair<LocalDate, LocalDate> {
            var resultStart = LocalDate.of(year, 1, 1)
            var resultEnd = LocalDate.of(year, 12, 31)

            if (start.year < year) {
                resultEnd = if (end > resultStart) {
                    if (end >= resultEnd) resultEnd else end
                } else if (end == resultStart) {
                    LocalDate.of(year, 1, 1)
                } else {
                    throw YearOutOfRangeException()
                }
                return Pair(resultStart, resultEnd)
            }

            if (start <= resultEnd) {
                resultStart = start
                resultEnd = if (end > resultEnd) {
                    resultEnd
                } else if (end < resultEnd) {
                    end
                } else {
                    throw YearOutOfRangeException()
                }
                return Pair(resultStart, resultEnd)
            }

            throw YearOutOfRangeException()
        }

        fun isDateInAssistancePlan(date: LocalDate, assistancePlanDto: AssistancePlanDto): Boolean {
            return !date.isBefore(assistancePlanDto.start) && !date.isAfter(assistancePlanDto.end)
        }

        fun isYearMonthInBetweenInclusive(yearMonth: YearMonth, start: LocalDate, end: LocalDate): Boolean {
            val endYearMonth = YearMonth.of(end.year, end.monthValue)
            val startYearMonth = YearMonth.of(start.year, start.monthValue)
            return (yearMonth.isBefore(endYearMonth) || yearMonth == endYearMonth) &&
                    (yearMonth.isAfter(startYearMonth) || yearMonth == startYearMonth)
        }

        fun containsStartAndEndASpecificYearMonth(start: LocalDate, end: LocalDate, yearMonth: YearMonth): Boolean {
            val startYearMonth = YearMonth.of(start.year, start.month)
            val endYearMonth = YearMonth.of(end.year, end.month)

            if (startYearMonth > yearMonth || endYearMonth < yearMonth) {
                return false
            }

            return startYearMonth <= yearMonth && endYearMonth >= yearMonth
        }

        fun countDaysOfAssistancePlan(year: Int, month: Int?, assistancePlanDto: AssistancePlanDto): Long {
            var start: LocalDate
            var end: LocalDate

            if (month != null) {
                start = LocalDate.of(year, month, 1)
                end = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth())
            } else {
                start = LocalDate.of(year, 1, 1)
                end = LocalDate.of(year, 12, 31)
            }

            // not in this month
            if (isAssistancePlanBetweenStartAndEnd(assistancePlanDto, end, start)) return 0

            start = if (assistancePlanDto.start < start) start else assistancePlanDto.start
            end = if (assistancePlanDto.end > end) end else assistancePlanDto.end

            return ChronoUnit.DAYS.between(start, end) + 1
        }

        fun countDaysOfYear(year: Int): Long {
            return if (Year.of(year).isLeap) 366L else 365L
        }

        fun countDaysOfYearBetweenStartAndEnd(year: Int, start: LocalDate, end: LocalDate?): Int {
            val startYear = LocalDate.of(year, 1, 1)
            val startReal = if (start < startYear) startYear else start
            val endReal = end ?: LocalDate.of(year, 12, 31)

            return (ChronoUnit.DAYS.between(startReal, endReal) + 1).toInt()
        }

        fun countDaysOfMonthAndYearBetweenStartAndEnd(year: Int, month: Int, start: LocalDate, end: LocalDate): Int {
            val calcStart = LocalDate.of(year, month, 1)
            val calcEnd = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

            if (start > calcEnd || end < calcStart) {
                return 0
            }

            if (start == calcEnd || end == calcStart) {
                return 1
            }

            if (start < calcStart && end > calcEnd) {
                return calcEnd.dayOfMonth
            }

            if (start >= calcStart && end <= calcEnd) {
                return (ChronoUnit.DAYS.between(start, end) + 1).toInt()
            }

            if (start >= calcStart) {
                return (ChronoUnit.DAYS.between(start, calcEnd) + 1).toInt()
            }

            if (end <= calcEnd) {
                return (ChronoUnit.DAYS.between(calcStart, end) + 1).toInt()
            }

            return 0
        }

        fun countWorkDaysOfMonthAndYearBetweenStartAndEnd(year: Int, month: Int, start: LocalDate, end: LocalDate): Int {
            val calcStart = LocalDate.of(year, month, 1)
            val calcEnd = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

            if (start > calcEnd || end < calcStart) {
                return 0
            }

            if (start == calcEnd || end == calcStart) {
                return if (isWorkday(start)) 1 else 0
            }

            if (start < calcStart && end > calcEnd) {
                return calculateWorkdaysInHesseBetween(calcStart, calcEnd)
            }

            if (start >= calcStart && end <= calcEnd) {
                return calculateWorkdaysInHesseBetween(start, end)
            }

            if (start >= calcStart) {
                return calculateWorkdaysInHesseBetween(start, calcEnd)
            }

            if (end <= calcEnd) {
                return calculateWorkdaysInHesseBetween(calcStart, end)
            }

            return 0
        }

        fun countDaysOfAssistancePlan(year: Int, assistancePlanDto: AssistancePlanDto): Long {
            return countDaysOfAssistancePlan(year, null, assistancePlanDto)
        }

        private fun isAssistancePlanBetweenStartAndEnd(
            assistancePlanDto: AssistancePlanDto,
            start: LocalDate,
            end: LocalDate
        ): Boolean {
            return assistancePlanDto.start > start || assistancePlanDto.end < end
        }

        fun convertMinutesToHour(minutes: Double): Double {
            val minutesPart = minutes % 60
            val hoursPart = (minutes - minutesPart) / 60

            return hoursPart + (minutesPart / 100.0)
        }

        fun convertHourToMinutes(hour: Double): Int {
            val hours = hour.toInt()
            val minutesPart = ((hour - hours) * 100).toInt()

            return hours * 60 + minutesPart
        }

        fun calculateWorkdaysInHesse(year: Int): Int {
            return calculateWorkdaysInHesseBetween(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), year)
        }

        fun calculateWorkdaysInHesseBetween(startDate: LocalDate, endDate: LocalDate): Int {
            var workdays = 0

            var currentDate = startDate
            var holidays = getHesseHolidays(startDate.year)
            while (!currentDate.isAfter(endDate)) {
                if (currentDate.year != holidays.first().year) {
                    holidays = getHesseHolidays(currentDate.year)
                }

                if (!isWeekend(currentDate) && !isHoliday(currentDate, holidays)) {
                    workdays++
                }

                currentDate = currentDate.plusDays(1)
            }
            return workdays
        }

        fun calculateWorkdaysInHesseBetween(startDate: LocalDate, endDate: LocalDate?, year: Int): Int {
            var workdays = 0
            val holidays = getHesseHolidays(startDate.year)

            val startYear = LocalDate.of(year, 1, 1)
            val startReal = if (startDate < startYear) startYear else startDate
            val endReal = endDate ?: LocalDate.of(year, 12, 31)

            // Alle Tage zwischen startDate und endDate durchlaufen
            var currentDate = startReal
            while (!currentDate.isAfter(endReal)) {
                // Wenn es kein Wochenende und kein Feiertag ist, zähle ihn als Arbeitstag
                if (!isWeekend(currentDate) && !isHoliday(currentDate, holidays)) {
                    workdays++
                }
                // Zum nächsten Tag wechseln
                currentDate = currentDate.plusDays(1)
            }
            return workdays
        }

        fun isWorkday(date: LocalDate): Boolean {
            val holidays = getHesseHolidays(date.year)
            return !holidays.contains(date) && !isWeekend(date)
        }

        private fun isWeekend(date: LocalDate): Boolean {
            return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        }

        private fun isHoliday(date: LocalDate, holidays: List<LocalDate>): Boolean {
            return holidays.contains(date)
        }

        private fun getHesseHolidays(year: Int): List<LocalDate> {
            val easterSunday = getEasterSunday(year)
            return listOf(
                LocalDate.of(year, Month.JANUARY, 1),   // Neujahr
                LocalDate.of(year, Month.MAY, 1),       // Tag der Arbeit
                easterSunday.minusDays(2),  // Karfreitag
                easterSunday.plusDays(1),      // Ostermontag
                LocalDate.of(year, Month.OCTOBER, 3),   // Tag der Deutschen Einheit
                easterSunday.plusDays(39),     // Christi Himmelfahrt
                easterSunday.plusDays(50),     // Pfingstmontag
                LocalDate.of(year, Month.NOVEMBER, 1),  // Allerheiligen
                LocalDate.of(year, Month.DECEMBER, 25), // 1. Weihnachtsfeiertag
                LocalDate.of(year, Month.DECEMBER, 26)  // 2. Weihnachtsfeiertag
            )
        }

        private fun getEasterSunday(year: Int): LocalDate {
            val a = year % 19
            val b = year / 100
            val c = year % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = ((h + l - 7 * m + 114) % 31) + 1
            return LocalDate.of(year, month, day)
        }
    }
}