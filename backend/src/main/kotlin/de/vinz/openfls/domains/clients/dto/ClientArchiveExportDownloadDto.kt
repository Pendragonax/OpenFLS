package de.vinz.openfls.domains.clients.dto

data class ClientArchiveExportDownloadDto(
    var fileName: String = "",
    var content: ByteArray = byteArrayOf()
)
