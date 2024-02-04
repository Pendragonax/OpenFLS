package de.vinz.openfls.services.models

import java.time.LocalDate

data class DateRangeArgument(val start: LocalDate,
                             val end: LocalDate,
                             val year: Int,
                             val month: Int,
                             val expectedDays: Long)
