package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode

import java.time.LocalDate

interface AssistancePlanProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val client: ClientSoloProjection
    val sponsor: AssistancePlanSponsorProjection
    val institution: AssistancePlanInstitutionProjection
    val hourMode: AssistancePlanHourMode
    val hourCorridor: AssistancePlanHourCorridorProjection?
    val hours: List<AssistancePlanHourProjection>
    val goals: List<AssistancePlanGoalProjection>
}
