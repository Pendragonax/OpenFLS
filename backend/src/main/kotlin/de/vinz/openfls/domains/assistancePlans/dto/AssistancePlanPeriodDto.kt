package de.vinz.openfls.domains.assistancePlans.dto

import java.time.LocalDate

/**
 * Minimal period information of an assistance plan, used where only the runtime of
 * a plan matters (for example the favourite client list).
 */
data class AssistancePlanPeriodDto(
    val id: Long,
    val clientId: Long,
    val start: LocalDate,
    val end: LocalDate
)
