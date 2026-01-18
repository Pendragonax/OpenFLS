package de.vinz.openfls.domains.permissions

class PermissionDto {
    var employeeId: Long = 0

    var institutionId: Long = 0

    var writeEntries: Boolean = false

    var readEntries: Boolean = false

    var changeInstitution: Boolean = false

    var affiliated: Boolean = false

    companion object {
        fun of(permission: Permission): PermissionDto {
            return PermissionDto().apply {
                this.employeeId = permission.id.employeeId ?: 0
                this.institutionId = permission.id.institutionId ?: 0
                this.writeEntries = permission.writeEntries
                this.readEntries = permission.readEntries
                this.changeInstitution = permission.changeInstitution
                this.affiliated = permission.affiliated
            }
        }
    }
}