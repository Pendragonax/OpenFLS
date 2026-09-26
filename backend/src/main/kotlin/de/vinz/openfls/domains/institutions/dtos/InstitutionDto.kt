package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection

data class InstitutionDto(
        var id: Long = 0,
        var name: String = "",
        var email: String = "",
        var phonenumber: String = ""
) {
        companion object {
                fun of(institution: Institution): InstitutionDto {
                        return InstitutionDto(
                                id = institution.id ?: 0,
                                name = institution.name,
                                email = institution.email,
                                phonenumber = institution.phonenumber
                        )
                }

                fun of(institutions: List<Institution>): List<InstitutionDto> {
                        return institutions.map { of(it) }
                }

                fun ofSoloProjection(institutionSoloProjection: InstitutionSoloProjection): InstitutionDto {
                        return InstitutionDto(
                                id = institutionSoloProjection.id,
                                name = institutionSoloProjection.name,
                                email = institutionSoloProjection.email,
                                phonenumber = institutionSoloProjection.phonenumber
                        )
                }

                fun ofSoloProjection(institutionSoloProjections: List<InstitutionSoloProjection>): List<InstitutionDto> {
                        return institutionSoloProjections.map { ofSoloProjection(it) }
                }
        }
}
