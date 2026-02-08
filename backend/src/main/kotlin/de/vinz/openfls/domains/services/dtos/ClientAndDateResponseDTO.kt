package de.vinz.openfls.domains.services.dtos

import de.vinz.openfls.domains.services.projections.FromTillEmployeeServiceProjection

data class ClientAndDateResponseDTO(
    val clientId: Long,
    val services: List<ClientAndDateServiceDTO>
) {
    data class ClientAndDateServiceDTO(
        val timepoint: String,
        val employeeName: String
    )

    companion object {
        fun of(clientId: Long, services: List<FromTillEmployeeServiceProjection>): ClientAndDateResponseDTO {
            return ClientAndDateResponseDTO(
                clientId = clientId,
                services = services.map { service ->
                    ClientAndDateServiceDTO(
                        timepoint = "${service.start.toLocalTime()} - ${service.end.toLocalTime()}",
                        employeeName = "${service.employeeFirstname.first()}. ${service.employeeLastname}"
                    )
                }
            )
        }
    }
}
