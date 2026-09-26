package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.hourCorridors.projections.HourCorridorSoloProjection
import de.vinz.openfls.domains.goals.projections.GoalProjection
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection

import java.time.LocalDate

interface AssistancePlanProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val client: ClientSoloProjection
    val sponsor: AssistancePlanSponsorProjection
    val institution: InstitutionSoloProjection
    val hourMode: AssistancePlanHourMode
    val hourCorridor: HourCorridorSoloProjection?
    val hours: List<AssistancePlanHourProjection>
    val goals: List<GoalProjection>
}
