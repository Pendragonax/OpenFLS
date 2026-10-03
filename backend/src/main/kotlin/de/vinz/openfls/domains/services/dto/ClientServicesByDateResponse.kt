package de.vinz.openfls.domains.services.dto

data class ClientServicesByDateResponse(
    val clientId: Long,
    val services: List<ClientServicesByDateEntry>
) {
    data class ClientServicesByDateEntry(
        val id: Long,
        val timepoint: String,
        val employeeName: String
    )
}
