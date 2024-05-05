package de.vinz.openfls.projections

import java.time.LocalDateTime

interface ServiceSoloProjection {
    val id: Long
    val start: LocalDateTime
    val end: LocalDateTime
    val minutes: Int
    val title: String
    val content: String
    val unfinished: Boolean
}