package de.vinz.openfls.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.validation.constraints.NotNull

class ClientSimpleDto {
    var id: Long = 0

    var firstName: String = ""

    var lastName: String = ""

    var phoneNumber: String = ""

    var email: String = ""

    @JsonIgnoreProperties(value = ["contingents", "permissions", "assistancePlans", "goals", "hibernateLazyInitializer"])
    var institution: InstitutionDto = InstitutionDto()
}