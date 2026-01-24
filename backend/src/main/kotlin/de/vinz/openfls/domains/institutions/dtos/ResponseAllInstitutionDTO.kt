package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.permissions.PermissionDto

data class ResponseAllInstitutionDTO(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phonenumber: String = "",
    val permissions: List<PermissionDto> = emptyList()
) {
    companion object {
        fun of(entity: Institution): ResponseAllInstitutionDTO {
            return ResponseAllInstitutionDTO(
                id = entity.id ?: 0,
                name = entity.name,
                email = entity.email,
                phonenumber = entity.phonenumber,
                permissions = entity.permissions?.map { PermissionDto.of(it) } ?: emptyList()
            )
        }
    }
}
