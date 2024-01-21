package de.vinz.openfls.models

import java.time.LocalDate
import java.time.YearMonth

class YearMonthDoubleValue(
        val yearMonth: YearMonth,
        var value: Double
) {
    companion object {
        fun getEmpty(start: LocalDate, end: LocalDate): List<YearMonthDoubleValue> {
            if (start > end) {
                return emptyList()
            }

            var actualDate = LocalDate.of(start.year, start.monthValue, start.dayOfMonth)
            val resultList = mutableListOf<YearMonthDoubleValue>()

            while(actualDate < end) {
                resultList.add(YearMonthDoubleValue(YearMonth.of(actualDate.year, actualDate.monthValue), 0.0))
                actualDate = actualDate.plusMonths(1)
            }

            return resultList.sortedBy { it.yearMonth }
        }
    }
}