package de.vinz.openfls.domains.clients.dto

sealed class ClientDeleteResult {
    data class Success(val response: ClientDetailResponse) : ClientDeleteResult()
    data object NotFound : ClientDeleteResult()
    data object Archived : ClientDeleteResult()
}
