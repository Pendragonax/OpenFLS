package de.vinz.openfls.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class ClientDto {
    var id: Long = 0

    @field:NotBlank
    var firstName: String = ""

    @field:NotBlank
    var lastName: String = ""

    var phoneNumber: String = ""

    var email: String = ""

    @field:NotNull
    var categoryTemplate: CategoryTemplateDto = CategoryTemplateDto()

    @field:NotNull
    @JsonIgnoreProperties(value = ["contingents", "permissions", "assistancePlans", "goals", "hibernateLazyInitializer"])
    var institution: InstitutionDto = InstitutionDto()

    @JsonIgnoreProperties(value = ["client", "services", "hibernateLazyInitializer"])
    var assistancePlans: Array<AssistancePlanDto> = emptyArray()
}
