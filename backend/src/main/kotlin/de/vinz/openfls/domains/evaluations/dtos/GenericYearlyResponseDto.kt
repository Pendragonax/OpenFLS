package de.vinz.openfls.domains.evaluations.dtos

import java.time.LocalDate

class GenericYearlyResponseDto<T> {
    var year: Int = LocalDate.now().year

    var values: List<T> = emptyList()
}