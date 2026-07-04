package de.vinz.openfls.domains.clients.archive.export.dtos

import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportFormat
import java.time.LocalDateTime

class ClientArchiveExportStatusDto {
    var ready: Boolean = false
    var format: ClientArchiveExportFormat = ClientArchiveExportFormat.JSON
    var requestedAt: LocalDateTime? = null
    var requestedByEmployeeId: Long = 0
    var downloadLink: ClientArchiveExportDownloadLinkDto? = null
}
