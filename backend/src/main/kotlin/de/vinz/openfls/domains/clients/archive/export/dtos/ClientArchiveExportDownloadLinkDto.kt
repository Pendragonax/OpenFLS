package de.vinz.openfls.domains.clients.archive.export.dtos

import java.time.LocalDateTime

class ClientArchiveExportDownloadLinkDto {
    var downloadLink: String = ""
    var downloadLinkExpiresAt: LocalDateTime? = null
    var downloadedAt: LocalDateTime? = null
}
