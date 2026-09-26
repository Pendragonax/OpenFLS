package de.vinz.openfls.domains.clients.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class ClientCreateDto {
    @field:NotBlank
    var firstName: String = ""

    @field:NotBlank
    var lastName: String = ""

    var phoneNumber: String = ""

    var email: String = ""

    @field:NotNull
    var institutionId: Long = 0

    @field:NotNull
    var categoryTemplateId: Long = 0
}
