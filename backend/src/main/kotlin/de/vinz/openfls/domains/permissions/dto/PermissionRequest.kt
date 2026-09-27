package de.vinz.openfls.domains.permissions.dto

data class PermissionRequest(
    val employeeId: Long = 0,
    val institutionId: Long = 0,
    val readEntries: Boolean = false,
    val writeEntries: Boolean = false,
    val changeInstitution: Boolean = false,
    val affiliated: Boolean = false
)
