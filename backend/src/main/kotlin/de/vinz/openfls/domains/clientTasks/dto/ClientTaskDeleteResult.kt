package de.vinz.openfls.domains.clientTasks.dto

sealed class ClientTaskDeleteResult {
    data class Success(val response: ClientTaskResponse) : ClientTaskDeleteResult()
    data object NotFound : ClientTaskDeleteResult()
}
