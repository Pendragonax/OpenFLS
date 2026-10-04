package de.vinz.openfls.domains.clients.dto

import de.vinz.openfls.domains.clients.entity.ClientArchiveExportFormat
import jakarta.validation.constraints.NotNull

class ClientArchiveExportRequest {
    @field:NotNull
    var format: ClientArchiveExportFormat? = ClientArchiveExportFormat.JSON
    var anonymize: Boolean = false
}
