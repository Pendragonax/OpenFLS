package de.vinz.openfls.projections

interface SponsorSoloProjection {
    val id: Long
    val name: String
    val payOverhang: Boolean
    val payExact: Boolean
}