package de.vinz.openfls.domains.clients.dto.export

import de.vinz.openfls.domains.institutions.entity.Institution

data class ClientArchiveExportInstitutionDto(
    var id: Long = 0,
    var name: String = ""
) {
    companion object {
        fun from(institution: Institution): ClientArchiveExportInstitutionDto {
            return ClientArchiveExportInstitutionDto(
                id = institution.id ?: 0,
                name = institution.name
            )
        }
    }
}
