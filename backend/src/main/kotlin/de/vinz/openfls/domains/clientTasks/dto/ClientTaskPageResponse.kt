package de.vinz.openfls.domains.clientTasks.dto

data class ClientTaskPageResponse(
    val content: List<ClientTaskResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
