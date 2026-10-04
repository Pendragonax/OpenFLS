package de.vinz.openfls.domains.services.projection

import java.time.LocalDateTime

interface ServiceCalendarProjection {
    val id: Long
    val start: LocalDateTime
    val minutes: Int
}