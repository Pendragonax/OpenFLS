package de.vinz.openfls.domains.clients.archive.export.dtos

data class ClientArchiveExportDownloadDto(
    var fileName: String = "",
    var content: ByteArray = byteArrayOf()
)
