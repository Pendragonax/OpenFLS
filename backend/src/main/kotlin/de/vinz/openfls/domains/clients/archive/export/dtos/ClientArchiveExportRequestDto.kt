package de.vinz.openfls.domains.clients.archive.export.dtos

import de.vinz.openfls.domains.clients.archive.export.ClientArchiveExportFormat
import jakarta.validation.constraints.NotNull

class ClientArchiveExportRequestDto {
    @field:NotNull
    var format: ClientArchiveExportFormat? = ClientArchiveExportFormat.JSON
    var anonymize: Boolean = false
}
