package de.vinz.openfls.services

import de.vinz.openfls.domains.services.dtos.ServiceDto
import de.vinz.openfls.domains.services.dtos.ServiceTimeDayDto
import de.vinz.openfls.domains.services.dtos.ServiceTimeDto
import org.springframework.stereotype.Service

@Service
class ConverterService {
    /**
     * This method convert the [List] of [de.vinz.openfls.entities.Service] into [ServiceTimeDto]
     * @param services - [List] [de.vinz.openfls.entities.Service]
     * @return [ServiceTimeDto]
     */
    fun convertServicesToServiceTimeDto(services: List<ServiceDto>): ServiceTimeDto {
        if (services.isEmpty())
            return ServiceTimeDto()

        val serviceTimeDto = ServiceTimeDto()
        serviceTimeDto.days = services
            .groupBy { it.start.toLocalDate() }
            .map { ServiceTimeDayDto().apply {
                this.date = it.key
                this.serviceCount = it.value.size
                this.hours = it.value.sumOf { service -> service.minutes } / 60.0
            } }
            .toMutableSet()

        return serviceTimeDto
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