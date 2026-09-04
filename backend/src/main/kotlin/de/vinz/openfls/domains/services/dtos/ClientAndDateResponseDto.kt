package de.vinz.openfls.domains.services.dtos

import de.vinz.openfls.domains.services.projections.FromTillEmployeeServiceProjection

data class ClientAndDateResponseDto(
    val clientId: Long,
    val services: List<ClientAndDateServiceDto>
) {
    data class ClientAndDateServiceDto(
        val id: Long,
        val timepoint: String,
        val employeeName: String
    )

    companion object {
        fun of(clientId: Long, services: List<FromTillEmployeeServiceProjection>): ClientAndDateResponseDto {
            return ClientAndDateResponseDto(
                clientId = clientId,
                services = services.map { service ->
                    ClientAndDateServiceDto(
                        id = service.id,
                        timepoint = "${service.start.toLocalTime()} - ${service.end.toLocalTime()}",
                        employeeName = "${service.employeeFirstname.first()}. ${service.employeeLastname}"
                    )
                }
            )
        }
    }
}
