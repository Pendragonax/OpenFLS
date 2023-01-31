package de.vinz.openfls.services

import de.vinz.openfls.dtos.ServiceTimeDayDto
import de.vinz.openfls.dtos.ServiceTimeDto
import org.springframework.stereotype.Service

@Service
class ConverterService {
    /**
     * This method convert the [List] of [de.vinz.openfls.model.Service] into [ServiceTimeDto]
     * @param services - [List] [de.vinz.openfls.model.Service]
     * @return [ServiceTimeDto]
     */
    fun convertServiceDTOsToServiceTimeDto(services: List<de.vinz.openfls.model.Service>): ServiceTimeDto {
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
}