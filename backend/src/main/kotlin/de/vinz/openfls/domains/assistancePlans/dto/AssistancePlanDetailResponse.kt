package de.vinz.openfls.domains.assistancePlans.dto

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import java.time.LocalDate

/**
 * Plan with client, sponsor, institution, hour corridor, hours and goals for the analysis page.
 * It is built completely inside the service transaction, so no lazy entities leave the service.
 */
data class AssistancePlanDetailResponse(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val client: AssistancePlanDetailClientResponse,
    val sponsor: AssistancePlanDetailSponsorResponse,
    val institution: InstitutionResponse,
    val hourMode: AssistancePlanHourMode,
    val hourCorridor: AssistancePlanDetailHourCorridorResponse?,
    val hours: List<AssistancePlanDetailHourResponse>,
    val goals: List<AssistancePlanDetailGoalResponse>
) {
    companion object {
        fun from(entity: AssistancePlan): AssistancePlanDetailResponse {
            val client = entity.client
                ?: throw IllegalStateException("assistance plan [id=${entity.id}] has no client")
            val sponsor = entity.sponsor
                ?: throw IllegalStateException("assistance plan [id=${entity.id}] has no sponsor")
            val institution = entity.institution
                ?: throw IllegalStateException("assistance plan [id=${entity.id}] has no institution")

            return AssistancePlanDetailResponse(
                id = entity.id,
                start = entity.start,
                end = entity.end,
                client = AssistancePlanDetailClientResponse.from(client),
                sponsor = AssistancePlanDetailSponsorResponse.from(sponsor),
                institution = InstitutionResponse(
                    id = institution.id ?: 0,
                    name = institution.name,
                    email = institution.email,
                    phonenumber = institution.phonenumber
                ),
                hourMode = entity.hourMode,
                hourCorridor = entity.hourCorridor?.let { AssistancePlanDetailHourCorridorResponse.from(it) },
                hours = entity.hours
                    .sortedBy { it.id }
                    .map { AssistancePlanDetailHourResponse.from(it, entity) },
                goals = entity.goals
                    .sortedBy { it.id }
                    .map { AssistancePlanDetailGoalResponse.from(it, entity.id) }
            )
        }
    }
}
