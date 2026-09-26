package de.vinz.openfls.domains.hourCorridors.projections

/**
 * Schlanke Projection eines [de.vinz.openfls.domains.hourCorridors.HourCorridor] für die
 * Verwendung innerhalb anderer Projections. Ersetzt die frühere direkte Einbettung der
 * JPA-Entity, damit keine Entity die Service-Grenze verlässt.
 */
interface HourCorridorSoloProjection {
    val id: Long
    val title: String
    val weeklyMinutesFrom: Int
    val weeklyMinutesTill: Int
    val hourType: HourCorridorHourTypeProjection?
}
