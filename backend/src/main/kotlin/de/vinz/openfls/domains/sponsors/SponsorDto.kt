package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto

class SponsorDto {
    var id: Long = 0
    var name: String = ""
    var payOverhang: Boolean = false
    var payExact: Boolean = false
    var unprofessionals: Array<UnprofessionalDto>? = null
}