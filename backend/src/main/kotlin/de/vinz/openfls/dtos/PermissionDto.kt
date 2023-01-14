package de.vinz.openfls.dtos

class PermissionDto {
    var employeeId: Long = 0

    var institutionId: Long = 0

    var writeEntries: Boolean = false

    var readEntries: Boolean = false

    var changeInstitution: Boolean = false

    var affiliated: Boolean = false
}