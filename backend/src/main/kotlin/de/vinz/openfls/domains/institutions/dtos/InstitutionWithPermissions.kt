package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.permissions.PermissionDto
import jakarta.validation.constraints.NotEmpty

/**
 * Rückgabetyp von create/update sowie von getAll/getById im
 * [de.vinz.openfls.domains.institutions.InstitutionService], wenn die Permissions
 * der Institution benötigt werden.
 */
class InstitutionWithPermissions {
    var id: Long = 0

    @field:NotEmpty
    var name: String = ""

    var email: String = ""

    var phonenumber: String = ""

    var permissions: List<PermissionDto> = emptyList()

    constructor(id: Long, name: String, email: String, phonenumber: String) {
        this.id = id
        this.name = name
        this.email = email
        this.phonenumber = phonenumber
    }

    constructor()

    companion object {
        fun of(entity: Institution): InstitutionWithPermissions {
            return InstitutionWithPermissions(
                id = entity.id ?: 0,
                name = entity.name,
                email = entity.email,
                phonenumber = entity.phonenumber
            ).apply {
                permissions = entity.permissions.map { PermissionDto.of(it) }
            }
        }
    }
}
