package de.vinz.openfls.domains.institutions.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class InstitutionUpdateRequest(
    val id: Long = 0,
    @field:NotEmpty
    val name: String = "",
    val email: String = "",
    val phonenumber: String = "",
    @field:Valid
    val permissions: List<InstitutionPermissionRequest> = emptyList()
)
