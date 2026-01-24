package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.permissions.PermissionDto

data class CreateInstitutionDTO(
    var name: String = "",
    var email: String = "",
    var phonenumber: String = "",
    var permissions: List<PermissionDto> = listOf()
) {
    companion object {
        fun of(institution: Institution): CreateInstitutionDTO {
            return CreateInstitutionDTO(
                name = institution.name,
                email = institution.email,
                phonenumber = institution.phonenumber,
                permissions = institution.permissions?.map { PermissionDto.of(it) } ?: listOf()
            )
        }
    }
}