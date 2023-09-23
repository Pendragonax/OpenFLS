package de.vinz.openfls.services

import de.vinz.openfls.dtos.AssistancePlanDto
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class DateService {
    companion object {
        fun isDateInAssistancePlan(date: LocalDate, assistancePlanDto: AssistancePlanDto): Boolean {
            return !date.isBefore(assistancePlanDto.start) && !date.isAfter(assistancePlanDto.end)
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

        fun countDaysOfAssistancePlan(year: Int, assistancePlanDto: AssistancePlanDto): Long {
            return countDaysOfAssistancePlan(year, null, assistancePlanDto)
        }

        private fun isAssistancePlanBetweenStartAndEnd(assistancePlanDto: AssistancePlanDto,
                                                       start: LocalDate,
                                                       end: LocalDate): Boolean {
            return assistancePlanDto.start > start || assistancePlanDto.end < end
        }
    }
}