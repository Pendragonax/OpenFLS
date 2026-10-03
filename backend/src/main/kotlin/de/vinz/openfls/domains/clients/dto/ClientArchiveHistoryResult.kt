package de.vinz.openfls.domains.clients.dto

sealed class ClientArchiveHistoryResult {
    data class Success(val entries: List<ClientArchiveHistoryEntryResponse>) : ClientArchiveHistoryResult()
    data object NotFound : ClientArchiveHistoryResult()
    data object Forbidden : ClientArchiveHistoryResult()
}
