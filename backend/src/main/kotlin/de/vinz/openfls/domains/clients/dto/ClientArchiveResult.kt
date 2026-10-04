package de.vinz.openfls.domains.clients.dto

sealed class ClientArchiveResult {
    data class Success(val response: ClientArchiveHistoryEntryResponse) : ClientArchiveResult()
    data object NotFound : ClientArchiveResult()
    data object Forbidden : ClientArchiveResult()
    data object ActorNotFound : ClientArchiveResult()
    data object AlreadyArchived : ClientArchiveResult()
    data object NotArchived : ClientArchiveResult()
}
