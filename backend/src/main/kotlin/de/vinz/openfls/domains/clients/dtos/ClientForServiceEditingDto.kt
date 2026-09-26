package de.vinz.openfls.domains.clients.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanForServiceEditingDto
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateWithCategoriesResponse
import de.vinz.openfls.domains.institutions.dtos.InstitutionDto

class ClientForServiceEditingDto {
    var id: Long = 0
    var firstName: String = ""
    var lastName: String = ""
    var phoneNumber: String = ""
    var email: String = ""
    var archived: Boolean = false
    var categoryTemplate: CategoryTemplateWithCategoriesResponse = CategoryTemplateWithCategoriesResponse()
    var institution: InstitutionDto = InstitutionDto()
    var assistancePlans: Array<AssistancePlanForServiceEditingDto> = emptyArray()
}

