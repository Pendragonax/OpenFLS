package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.permissions.PermissionDto
import jakarta.validation.constraints.NotEmpty

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
}