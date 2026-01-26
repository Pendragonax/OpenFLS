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

                        val minutes = it.value.sumOf { service -> service.minutes }
                        this.hours = minutes / 60
                        this.minutes = minutes % 60
                    } }
                    .toMutableSet()

            return serviceTimeDto
        }
    }
}