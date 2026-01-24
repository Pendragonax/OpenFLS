package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection

data class ResponseAllReadableInstitutionDTO(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phonenumber: String = ""
) {
    companion object {
        fun of(institution: Institution): ResponseAllReadableInstitutionDTO {
            return ResponseAllReadableInstitutionDTO(
                id = institution.id ?: 0,
                name = institution.name,
                email = institution.email,
                phonenumber = institution.phonenumber
            )
        }

        fun of(institutionSoloProjection: InstitutionSoloProjection): ResponseAllReadableInstitutionDTO {
            return ResponseAllReadableInstitutionDTO(
                id = institutionSoloProjection.id,
                name = institutionSoloProjection.name,
                email = institutionSoloProjection.email,
                phonenumber = institutionSoloProjection.phonenumber
            )
        }
    }
}