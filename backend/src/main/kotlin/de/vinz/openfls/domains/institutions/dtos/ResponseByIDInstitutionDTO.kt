package de.vinz.openfls.domains.institutions.dtos

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.permissions.PermissionDto

data class ResponseByIDInstitutionDTO(
    var id: Long = 0,
    var name: String = "",
    var email: String = "",
    var phonenumber: String = "",
    var permissions: List<PermissionDto> = emptyList(),
    var contingents: List<ContingentDto> = emptyList(),
    var assistancePlans: List<AssistancePlanDto> = emptyList(),
) {
}