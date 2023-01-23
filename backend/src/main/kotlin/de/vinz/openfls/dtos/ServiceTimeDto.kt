package de.vinz.openfls.dtos

class ServiceTimeDto {
    var days: MutableSet<ServiceTimeDayDto> = mutableSetOf()

    var periodDays: Int = 0
}