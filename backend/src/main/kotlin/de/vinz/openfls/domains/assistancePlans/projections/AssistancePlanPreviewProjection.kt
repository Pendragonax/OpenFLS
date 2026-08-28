package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import java.time.LocalDate

interface AssistancePlanPreviewProjection {
    val id: Long
    val clientId: Long
    val institutionId: Long
    val sponsorId: Long
    val clientFirstname: String
    val clientLastname: String
    val clientArchived: Boolean
    val institutionName: String
    val sponsorName: String
    val hourMode: AssistancePlanHourMode
    val hourCorridorWeeklyMinutesFrom: Int?
    val hourCorridorWeeklyMinutesTill: Int?
    val start: LocalDate
    val end: LocalDate
}
