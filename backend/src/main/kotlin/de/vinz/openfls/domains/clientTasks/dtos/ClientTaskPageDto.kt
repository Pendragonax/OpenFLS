package de.vinz.openfls.domains.clientTasks.dtos

data class ClientTaskPageDto(
    val content: List<ClientTaskDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
