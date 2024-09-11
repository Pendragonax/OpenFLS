package de.vinz.openfls.domains.services.dtos

import java.time.LocalDate

class ServiceTimeDayDto {
    var date: LocalDate = LocalDate.now()

    var hours: Double = 0.0

    var serviceCount: Int = 0
}