package de.vinz.openfls.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.YearOutOfRangeException
import java.time.LocalDate
import java.time.YearMonth
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
            end =  if (assistancePlanDto.end > end) end else assistancePlanDto.end

            return ChronoUnit.DAYS.between(start, end) + 1
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

        fun countDaysOfAssistancePlan(year: Int, assistancePlanDto: AssistancePlanDto): Long {
            return countDaysOfAssistancePlan(year, null, assistancePlanDto)
        }

        private fun isAssistancePlanBetweenStartAndEnd(assistancePlanDto: AssistancePlanDto,
                                                       start: LocalDate,
                                                       end: LocalDate): Boolean {
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
    }
}