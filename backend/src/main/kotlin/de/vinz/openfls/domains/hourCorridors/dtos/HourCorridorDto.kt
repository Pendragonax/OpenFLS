package de.vinz.openfls.domains.hourCorridors.dtos

import de.vinz.openfls.domains.hourCorridors.HourCorridor
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

data class HourCorridorDto(
    @field:PositiveOrZero
    var id: Long = 0,

    @field:NotEmpty
    var title: String = "",

    @field:PositiveOrZero
    var weeklyMinutesFrom: Int = 0,

    @field:PositiveOrZero
    var weeklyMinutesTill: Int = 0,

    @field:Positive
    var hourTypeId: Long = 0,

    var hourTypeTitle: String = "",

    var assistancePlanCount: Long = 0
) {
    companion object {
        fun from(hourCorridor: HourCorridor, assistancePlanCount: Long = 0): HourCorridorDto {
            return HourCorridorDto(
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
