package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection

data class InstitutionSoloDto(
        var id: Long = 0,
        var name: String = "",
        var email: String = "",
        var phonenumber: String = ""
) {
        companion object {
                fun of(institution: Institution): InstitutionSoloDto {
                        return InstitutionSoloDto(
                                id = institution.id ?: 0,
                                name = institution.name,
                                email = institution.email,
                                phonenumber = institution.phonenumber
                        )
                }

                fun of(institutions: List<Institution>): List<InstitutionSoloDto> {
                        return institutions.map { of(it) }
                }

                fun ofSoloProjection(institutionSoloProjection: InstitutionSoloProjection): InstitutionSoloDto {
                        return InstitutionSoloDto(
                                id = institutionSoloProjection.id ?: 0,
                                name = institutionSoloProjection.name,
                                email = institutionSoloProjection.email,
                                phonenumber = institutionSoloProjection.phonenumber
                        )
                }

                fun ofSoloProjection(institutionSoloProjections: List<InstitutionSoloProjection>): List<InstitutionSoloDto> {
                        return institutionSoloProjections.map { ofSoloProjection(it) }
                }
        }
}
