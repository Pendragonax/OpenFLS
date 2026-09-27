package de.vinz.openfls.domains.sponsors.dto

import de.vinz.openfls.domains.sponsors.entity.Sponsor

data class SponsorResponse(
    val id: Long = 0,
    val name: String = "",
    val payOverhang: Boolean = false,
    val payExact: Boolean = false
) {
    companion object {
        fun from(sponsor: Sponsor): SponsorResponse {
            return SponsorResponse(
                id = sponsor.id,
                name = sponsor.name,
                payOverhang = sponsor.payOverhang,
                payExact = sponsor.payExact
            )
        }
    }
}
