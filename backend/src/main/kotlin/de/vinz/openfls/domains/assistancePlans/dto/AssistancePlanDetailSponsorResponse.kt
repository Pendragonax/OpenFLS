package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.sponsors.entity.Sponsor

data class AssistancePlanDetailSponsorResponse(
    val id: Long,
    val name: String,
    val payOverhang: Boolean,
    val payExact: Boolean
) {
    companion object {
        fun from(sponsor: Sponsor): AssistancePlanDetailSponsorResponse {
            return AssistancePlanDetailSponsorResponse(
                id = sponsor.id,
                name = sponsor.name,
                payOverhang = sponsor.payOverhang,
                payExact = sponsor.payExact
            )
        }
    }
}
