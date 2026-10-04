package de.vinz.openfls.domains.services.projection

import java.time.LocalDateTime

interface ServiceProjection {
    val id: Long
    val start: LocalDateTime
    val end: LocalDateTime
    val minutes: Int
    val title: String
    val content: String
    val unfinished: Boolean
    val groupService: Boolean
}