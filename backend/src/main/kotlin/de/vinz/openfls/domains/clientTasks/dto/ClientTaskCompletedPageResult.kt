package de.vinz.openfls.domains.clientTasks.dto

sealed class ClientTaskCompletedPageResult {
    data class Success(val response: ClientTaskPageResponse) : ClientTaskCompletedPageResult()
    data object ClientNotFound : ClientTaskCompletedPageResult()
    data object InvalidPagination : ClientTaskCompletedPageResult()
}
