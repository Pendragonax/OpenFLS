package de.vinz.openfls.domains.hourCorridors.dto

import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor

data class HourCorridorResponse(
    val id: Long = 0,
    val title: String = "",
    val weeklyMinutesFrom: Int = 0,
    val weeklyMinutesTill: Int = 0,
    val hourTypeId: Long = 0,
    val hourTypeTitle: String = "",
    val assistancePlanCount: Long = 0
) {
    companion object {
        fun from(hourCorridor: HourCorridor, assistancePlanCount: Long): HourCorridorResponse {
            return HourCorridorResponse(
                id = hourCorridor.id,
                title = hourCorridor.title,
                weeklyMinutesFrom = hourCorridor.weeklyMinutesFrom,
                weeklyMinutesTill = hourCorridor.weeklyMinutesTill,
                hourTypeId = hourCorridor.hourType?.id ?: 0,
                hourTypeTitle = hourCorridor.hourType?.title ?: "",
                assistancePlanCount = assistancePlanCount
            )
        }
    }
}
