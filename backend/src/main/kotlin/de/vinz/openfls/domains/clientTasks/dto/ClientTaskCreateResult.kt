package de.vinz.openfls.domains.clientTasks.dto

sealed class ClientTaskCreateResult {
    data class Success(val response: ClientTaskResponse) : ClientTaskCreateResult()
    data object ClientNotFound : ClientTaskCreateResult()
}
