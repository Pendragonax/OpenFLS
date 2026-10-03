package de.vinz.openfls.domains.clients.dto

/**
 * Base data of a favourite client, without any assistance plan or task context.
 */
data class ClientFavoriteRowDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val archived: Boolean,
    val institutionId: Long,
    val institutionName: String
)
