package de.vinz.openfls.domains.clients.dto

sealed class ClientUpdateResult {
    data class Success(val response: ClientDetailResponse) : ClientUpdateResult()
    data object NotFound : ClientUpdateResult()
    data object Archived : ClientUpdateResult()
    data object InstitutionNotFound : ClientUpdateResult()
    data object CategoryTemplateNotFound : ClientUpdateResult()
}
