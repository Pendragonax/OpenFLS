package de.vinz.openfls.domains.clients.dto

import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import java.time.LocalDateTime

data class ClientArchiveExportStatusResponse(
    val ready: Boolean = false,
    val format: ClientArchiveExportFormat = ClientArchiveExportFormat.JSON,
    val requestedAt: LocalDateTime? = null,
    val requestedByEmployeeId: Long = 0,
    val downloadLink: ClientArchiveExportDownloadLinkResponse? = null
)
