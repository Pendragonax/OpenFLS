package de.vinz.openfls.domains.clients.dto

sealed class ClientArchiveExportStatusResult {
    data class Success(val response: ClientArchiveExportStatusResponse) : ClientArchiveExportStatusResult()
    data object NotFound : ClientArchiveExportStatusResult()
    data object Forbidden : ClientArchiveExportStatusResult()
}
