package de.vinz.openfls.domains.institutions.dto

import de.vinz.openfls.domains.permissions.Permission

data class InstitutionPermissionResponse(
    val employeeId: Long = 0,
    val institutionId: Long = 0,
    val writeEntries: Boolean = false,
    val readEntries: Boolean = false,
    val changeInstitution: Boolean = false,
    val affiliated: Boolean = false
) {
    companion object {
        fun from(permission: Permission): InstitutionPermissionResponse {
            return InstitutionPermissionResponse(
                employeeId = permission.id.employeeId ?: 0,
                institutionId = permission.id.institutionId ?: 0,
                writeEntries = permission.writeEntries,
                readEntries = permission.readEntries,
                changeInstitution = permission.changeInstitution,
                affiliated = permission.affiliated
            )
        }
    }
}
