package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.permissions.PermissionDto
import jakarta.validation.constraints.NotEmpty

/**
 * Kanonisches Lese-Dto einer Institution. Rückgabetyp von create/update sowie
 * von getAll/getById im [de.vinz.openfls.domains.institutions.InstitutionService].
 */
class InstitutionDto {
    var id: Long = 0

    @field:NotEmpty
    var name: String = ""

    var email: String = ""

    var phonenumber: String = ""

    var permissions: List<PermissionDto> = emptyList()

    var contingents: List<ContingentDto>? = emptyList()

    var assistancePlans: List<AssistancePlanDto>? = emptyList()

    constructor(id: Long, name: String, email: String, phonenumber: String) {
        this.id = id
        this.name = name
        this.email = email
        this.phonenumber = phonenumber
    }

    constructor()

    companion object {
        fun of(entity: Institution): InstitutionDto {
            return InstitutionDto(
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
