package de.vinz.openfls.domains.clients.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.institutions.dtos.InstitutionDto

class ClientSimpleDto {
    var id: Long = 0

    var firstName: String = ""

    var lastName: String = ""

    var phoneNumber: String = ""

    var email: String = ""

    @JsonIgnoreProperties(value = ["contingents", "permissions", "assistancePlans", "goals", "hibernateLazyInitializer"])
    var institution: InstitutionDto = InstitutionDto()
}