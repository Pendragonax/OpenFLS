package de.vinz.openfls.domains.permissions.dto

import de.vinz.openfls.domains.permissions.entity.Permission

data class PermissionResponse(
    val employeeId: Long = 0,
    val institutionId: Long = 0,
    val readEntries: Boolean = false,
    val writeEntries: Boolean = false,
    val changeInstitution: Boolean = false,
    val affiliated: Boolean = false
) {
    companion object {
        fun from(permission: Permission): PermissionResponse {
            return PermissionResponse(
                employeeId = permission.id.employeeId ?: 0,
                institutionId = permission.id.institutionId ?: 0,
                readEntries = permission.readEntries,
                writeEntries = permission.writeEntries,
                changeInstitution = permission.changeInstitution,
                affiliated = permission.affiliated
            )
        }
    }
}
