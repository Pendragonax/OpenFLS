package de.vinz.openfls.domains.hourCorridors.projections

interface HourCorridorUsageProjection {
    val hourCorridorId: Long
    val assistancePlanCount: Long
}
