package de.vinz.openfls.domains.institutions.dtos

data class InstitutionPermissionRequest(
    val employeeId: Long = 0,
    val writeEntries: Boolean = false,
    val readEntries: Boolean = false,
    val changeInstitution: Boolean = false,
    val affiliated: Boolean = false
)
