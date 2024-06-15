package de.vinz.openfls.domains.permissions

class PermissionDto {
    var employeeId: Long = 0

    var institutionId: Long = 0

    var writeEntries: Boolean = false

    var readEntries: Boolean = false

    var changeInstitution: Boolean = false

    var affiliated: Boolean = false
}