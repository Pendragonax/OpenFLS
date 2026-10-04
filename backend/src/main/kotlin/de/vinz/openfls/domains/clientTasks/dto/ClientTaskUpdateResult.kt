package de.vinz.openfls.domains.clientTasks.dto

sealed class ClientTaskUpdateResult {
    data class Success(val response: ClientTaskResponse) : ClientTaskUpdateResult()
    data object NotFound : ClientTaskUpdateResult()
    data object AlreadyCompleted : ClientTaskUpdateResult()
}
