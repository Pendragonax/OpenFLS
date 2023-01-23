package de.vinz.openfls.dtos

import java.time.LocalDate

class ServiceTimeDayDto {
    var date: LocalDate = LocalDate.now()

    var hours: Double = 0.0

    var serviceCount: Int = 0
}