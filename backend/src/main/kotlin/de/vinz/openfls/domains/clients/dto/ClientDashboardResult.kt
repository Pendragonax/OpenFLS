package de.vinz.openfls.domains.clients.dto

sealed class ClientDashboardResult {
    data class Success(val response: ClientDashboardResponse) : ClientDashboardResult()
    data object NotFound : ClientDashboardResult()
}
