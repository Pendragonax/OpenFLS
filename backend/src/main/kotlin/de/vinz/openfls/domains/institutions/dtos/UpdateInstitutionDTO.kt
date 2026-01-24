package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.permissions.PermissionDto

data class UpdateInstitutionDTO(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phonenumber: String = "",
    val permissions: List<PermissionDto> = emptyList()
) {
    companion object {
        fun of(institution: Institution): UpdateInstitutionDTO {
            return UpdateInstitutionDTO(
                name = institution.name,
                email = institution.email,
                phonenumber = institution.phonenumber,
                permissions = institution.permissions?.map { PermissionDto.of(it) } ?: listOf()
            )
        }
    }
}