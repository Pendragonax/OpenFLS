package de.vinz.openfls.domains.clients.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.institutions.dtos.InstitutionResponse
import jakarta.validation.constraints.NotBlank

class ClientDto {
    var id: Long = 0

    @field:NotBlank
    var firstName: String = ""

    @field:NotBlank
    var lastName: String = ""

    var phoneNumber: String = ""

    var email: String = ""

    var archived: Boolean = false

    @JsonIgnoreProperties(value = ["contingents", "permissions", "assistancePlans", "goals", "hibernateLazyInitializer"])
    var institution: InstitutionResponse = InstitutionResponse()

    var categoryTemplateId: Long = 0

    var categoryTemplateTitle: String = ""
}
