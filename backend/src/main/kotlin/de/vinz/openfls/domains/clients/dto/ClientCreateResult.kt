package de.vinz.openfls.domains.clients.dto

sealed class ClientCreateResult {
    data class Success(val response: ClientDetailResponse) : ClientCreateResult()
    data object InstitutionNotFound : ClientCreateResult()
    data object CategoryTemplateNotFound : ClientCreateResult()
}
