package de.vinz.openfls.domains.services.dto

import de.vinz.openfls.domains.services.projection.ServiceCalendarProjection
import java.time.LocalDateTime

data class ServiceCalendarDto(
    val id: Long,
    val start: LocalDateTime,
    val minutes: Int
) {
    companion object {
        fun from(projection: ServiceCalendarProjection): ServiceCalendarDto =
            ServiceCalendarDto(projection.id, projection.start, projection.minutes)
    }
}
