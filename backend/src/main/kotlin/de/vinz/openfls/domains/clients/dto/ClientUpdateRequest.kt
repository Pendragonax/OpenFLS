package de.vinz.openfls.domains.clients.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class ClientUpdateRequest {
    var id: Long = 0

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
