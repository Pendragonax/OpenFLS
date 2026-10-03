package de.vinz.openfls.domains.clientTasks.dto

sealed class ClientTaskCompleteResult {
    data class Success(val response: ClientTaskResponse) : ClientTaskCompleteResult()
    data object NotFound : ClientTaskCompleteResult()
    data object AlreadyCompleted : ClientTaskCompleteResult()
}
