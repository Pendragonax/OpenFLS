package de.vinz.openfls.domains.institutions.dto

import de.vinz.openfls.domains.institutions.entity.Institution

data class InstitutionResponse(
    var id: Long = 0,
    var name: String = "",
    var email: String = "",
    var phonenumber: String = ""
) {
    companion object {
        fun from(institution: Institution): InstitutionResponse {
            return InstitutionResponse(
                id = institution.id ?: 0,
                name = institution.name,
                email = institution.email,
                phonenumber = institution.phonenumber
            )
        }
    }
}
