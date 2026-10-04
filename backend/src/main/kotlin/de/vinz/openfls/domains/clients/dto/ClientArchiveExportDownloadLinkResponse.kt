package de.vinz.openfls.domains.clients.dto

import java.time.LocalDateTime

data class ClientArchiveExportDownloadLinkResponse(
    val downloadLink: String = "",
    val downloadLinkExpiresAt: LocalDateTime? = null,
    val downloadedAt: LocalDateTime? = null
)
