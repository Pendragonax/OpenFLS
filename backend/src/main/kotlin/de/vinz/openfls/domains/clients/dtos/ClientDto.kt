package de.vinz.openfls.domains.clients.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateDto
import de.vinz.openfls.domains.institutions.dtos.InstitutionDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

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
