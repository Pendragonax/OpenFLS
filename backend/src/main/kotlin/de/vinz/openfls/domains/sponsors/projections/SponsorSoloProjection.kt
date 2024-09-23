package de.vinz.openfls.domains.sponsors.projections

interface SponsorSoloProjection {
    val id: Long
    val name: String
    val payOverhang: Boolean
    val payExact: Boolean
}