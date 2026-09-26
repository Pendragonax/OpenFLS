package de.vinz.openfls.domains.sponsors.dtos

import de.vinz.openfls.domains.sponsors.Sponsor

data class SponsorWithUnprofessionalsResponse(
    val id: Long = 0,
    val name: String = "",
    val payOverhang: Boolean = false,
    val payExact: Boolean = false,
    val unprofessionals: List<SponsorUnprofessionalResponse> = emptyList()
) {
    companion object {
        fun from(sponsor: Sponsor): SponsorWithUnprofessionalsResponse {
            return SponsorWithUnprofessionalsResponse(
                id = sponsor.id,
                name = sponsor.name,
                payOverhang = sponsor.payOverhang,
                payExact = sponsor.payExact,
                unprofessionals = sponsor.unprofessionals.orEmpty().map {
                    SponsorUnprofessionalResponse(
                        employeeId = it.id?.employeeId ?: 0,
                        sponsorId = it.id?.sponsorId ?: 0,
                        end = it.end
                    )
                }
            )
        }
    }
}
