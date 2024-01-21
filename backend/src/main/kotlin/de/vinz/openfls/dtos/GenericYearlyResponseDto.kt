package de.vinz.openfls.dtos

import java.time.LocalDate

class GenericYearlyResponseDto<T> {
    var year: Int = LocalDate.now().year

    var values: List<T> = emptyList()
}