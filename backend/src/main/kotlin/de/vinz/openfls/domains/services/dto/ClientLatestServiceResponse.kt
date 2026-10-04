package de.vinz.openfls.domains.services.dto

import java.time.LocalDateTime

/**
 * Compact read model of a single entry for the client dashboard. It carries only
 * what the short overview shows: when it happened, what it was about and who
 * documented it.
 */
data class ClientLatestServiceResponse(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val minutes: Int,
    val title: String,
    val content: String,
    val institutionId: Long,
    val institutionName: String,
    val employeeId: Long,
    val employeeFirstname: String,
    val employeeLastname: String,
    val assistancePlanId: Long
)
