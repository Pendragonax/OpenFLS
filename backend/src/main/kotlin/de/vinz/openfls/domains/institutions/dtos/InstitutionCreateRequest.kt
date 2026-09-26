package de.vinz.openfls.domains.institutions.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class InstitutionCreateRequest(
    @field:NotEmpty
    val name: String = "",
    val email: String = "",
    val phonenumber: String = "",
    @field:Valid
    val permissions: List<InstitutionPermissionRequest> = emptyList()
)
