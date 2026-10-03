package de.vinz.openfls.domains.clients.dtos

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanForServiceEditingDto
import de.vinz.openfls.domains.categories.dto.CategoryTemplateWithCategoriesResponse
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse

class ClientForServiceEditingDto {
    var id: Long = 0
    var firstName: String = ""
    var lastName: String = ""
    var phoneNumber: String = ""
    var email: String = ""
    var archived: Boolean = false
    var categoryTemplate: CategoryTemplateWithCategoriesResponse = CategoryTemplateWithCategoriesResponse()
    var institution: InstitutionResponse = InstitutionResponse()
    var assistancePlans: Array<AssistancePlanForServiceEditingDto> = emptyArray()
}

