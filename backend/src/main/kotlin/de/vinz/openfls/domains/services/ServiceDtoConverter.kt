package de.vinz.openfls.domains.services

import de.vinz.openfls.domains.services.dtos.ServiceDto
import de.vinz.openfls.domains.services.dtos.ServiceTimeDayDto
import de.vinz.openfls.domains.services.dtos.ServiceTimeDto

class ServiceDtoConverter {
    companion object {
        /**
         * This method convert the [List] of [ServiceDto] into [ServiceTimeDto]
         * @param services - [List] [ServiceDto]
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
    }
}