package de.vinz.openfls.domains.institutions.dto

import de.vinz.openfls.domains.institutions.entity.Institution

data class InstitutionWithPermissionsResponse(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phonenumber: String = "",
    val permissions: List<InstitutionPermissionResponse> = emptyList()
) {
    companion object {
        fun from(institution: Institution): InstitutionWithPermissionsResponse {
            return InstitutionWithPermissionsResponse(
                id = institution.id ?: 0,
                name = institution.name,
                email = institution.email,
                phonenumber = institution.phonenumber,
                permissions = institution.permissions.map { InstitutionPermissionResponse.from(it) }
            )
        }
    }
}
