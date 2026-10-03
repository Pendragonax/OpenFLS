package de.vinz.openfls.domains.services.dto

import de.vinz.openfls.domains.services.projection.ServiceProjection
import java.time.LocalDateTime

/**
 * Flache Form eines Eintrags ohne Relationen. Wird zwischen Services ausgetauscht.
 */
data class ServiceDto(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val minutes: Int,
    val title: String,
    val content: String,
    val unfinished: Boolean,
    val groupService: Boolean
) {
    companion object {
        fun from(projection: ServiceProjection): ServiceDto = ServiceDto(
            id = projection.id,
            start = projection.start,
            end = projection.end,
            minutes = projection.minutes,
            title = projection.title,
            content = projection.content,
            unfinished = projection.unfinished,
            groupService = projection.groupService
        )
    }
}
