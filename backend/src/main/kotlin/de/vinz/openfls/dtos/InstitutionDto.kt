package de.vinz.openfls.dtos

import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import javax.validation.constraints.NotEmpty

class InstitutionDto {
    var id: Long = 0

    @field:NotEmpty
    var name: String = ""

    var email: String = ""

    var phonenumber: String = ""

    var permissions: Array<PermissionDto>? = null

    var contingents: Array<ContingentDto>? = null

    var assistancePlans: Array<AssistancePlanDto>? = null

    constructor(id: Long, name: String, email: String, phonenumber: String) {
        this.id = id
        this.name = name
        this.email = email
        this.phonenumber = phonenumber
    }

    constructor()
}