package de.vinz.openfls.domains.assistancePlans.projections

interface AssistancePlanSponsorProjection {
    val id: Long
    val name: String
    val payOverhang: Boolean
    val payExact: Boolean
}
