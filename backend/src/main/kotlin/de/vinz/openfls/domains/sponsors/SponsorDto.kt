package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto

data class SponsorDto(
    var id: Long = 0,
    var name: String = "",
    var payOverhang: Boolean = false,
    var payExact: Boolean = false,
    var unprofessionals: List<UnprofessionalDto>? = null) {

    companion object {
        fun from(sponsor: Sponsor): SponsorDto {
            val unprofessionals = sponsor.unprofessionals?.let { UnprofessionalDto.from(it.toList()) }

            return SponsorDto(id = sponsor.id,
                    name = sponsor.name,
                    payOverhang = sponsor.payOverhang,
                    payExact = sponsor.payExact,
                    unprofessionals = unprofessionals)
        }
    }
}