package de.vinz.openfls.domains.assistancePlans.projections

import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import de.vinz.openfls.domains.goals.projections.GoalProjection
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection
import de.vinz.openfls.domains.sponsors.projections.SponsorSoloProjection
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