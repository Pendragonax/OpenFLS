package de.vinz.openfls.domains.clients.dto

sealed class ClientArchiveExportDownloadResult {
    data class Success(val download: ClientArchiveExportDownloadDto) : ClientArchiveExportDownloadResult()
    data class Gone(val reason: String) : ClientArchiveExportDownloadResult()
}
