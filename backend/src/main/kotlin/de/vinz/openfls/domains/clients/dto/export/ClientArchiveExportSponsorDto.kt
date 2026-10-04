package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.sponsors.entity.Sponsor

data class ClientArchiveExportSponsorDto(
    var id: Long = 0,
    var name: String = ""
) {
    companion object {
        fun from(sponsor: Sponsor): ClientArchiveExportSponsorDto {
            return ClientArchiveExportSponsorDto(
                id = sponsor.id,
                name = sponsor.name
            )
        }
    }
}
