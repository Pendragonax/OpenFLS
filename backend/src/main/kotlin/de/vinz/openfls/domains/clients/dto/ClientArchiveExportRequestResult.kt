package de.vinz.openfls.domains.clients.dto

sealed class ClientArchiveExportRequestResult {
    data class Success(val response: ClientArchiveExportStatusResponse) : ClientArchiveExportRequestResult()
    data object NotFound : ClientArchiveExportRequestResult()
    data object Forbidden : ClientArchiveExportRequestResult()
    data object UnsupportedFormat : ClientArchiveExportRequestResult()
    data object ActorNotFound : ClientArchiveExportRequestResult()
}
