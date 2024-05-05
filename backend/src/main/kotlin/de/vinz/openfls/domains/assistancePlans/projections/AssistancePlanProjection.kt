package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.projections.ClientSoloProjection
import de.vinz.openfls.projections.GoalProjection
import de.vinz.openfls.projections.InstitutionSoloProjection
import de.vinz.openfls.projections.SponsorSoloProjection
import java.time.LocalDate

interface AssistancePlanProjection {
    val id: Long
    val start: LocalDate
    val end: LocalDate
    val client: ClientSoloProjection
    val sponsor: SponsorSoloProjection
    val institution: InstitutionSoloProjection
    val hours: List<AssistancePlanHourProjection>
    val goals: List<GoalProjection>
}