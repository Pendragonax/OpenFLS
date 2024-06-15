package de.vinz.openfls.domains.services.dtos

class ServiceTimeDto {
    var days: MutableSet<ServiceTimeDayDto> = mutableSetOf()

    var periodDays: Int = 0
}