package de.vinz.openfls.domains.assistancePlans.dtos

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.clients.dtos.ClientSoloDto
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.entity.GoalHour
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import java.time.LocalDate

/**
 * Zweckgebundenes DTO fuer den Endpunkt `GET /api/assistance_plans/projection/{id}`.
 *
 * Wird vollstaendig innerhalb der Service-Transaktion aus der Entity aufgebaut, damit keine
 * JPA-Entities bzw. Lazy-Proxies die Service-Grenze verlassen und beim JSON-Serialisieren
 * (nach Transaktionsende, `spring.jpa.open-in-view=false`) keine LazyInitializationException
 * mehr entstehen kann.
 */
data class AssistancePlanProjectionDto(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate,
    val client: ClientSoloDto,
    val sponsor: AssistancePlanSponsorSoloDto,
    val institution: InstitutionResponse,
    val hourMode: AssistancePlanHourMode,
    val hourCorridor: AssistancePlanHourCorridorSoloDto?,
    val hours: List<AssistancePlanHourSoloDto>,
    val goals: List<AssistancePlanGoalSoloDto>
) {
    companion object {
        fun of(entity: AssistancePlan): AssistancePlanProjectionDto {
            val client = entity.client
                ?: throw IllegalStateException("assistance plan [id=${entity.id}] has no client")
            val sponsor = entity.sponsor
                ?: throw IllegalStateException("assistance plan [id=${entity.id}] has no sponsor")
            val institution = entity.institution
                ?: throw IllegalStateException("assistance plan [id=${entity.id}] has no institution")

            return AssistancePlanProjectionDto(
                id = entity.id,
                start = entity.start,
                end = entity.end,
                client = ClientSoloDto(
                    id = client.id,
                    firstName = client.firstName,
                    lastName = client.lastName,
                    phoneNumber = client.phoneNumber,
                    email = client.email,
                    archived = client.archived
                ),
                sponsor = AssistancePlanSponsorSoloDto(
                    id = sponsor.id,
                    name = sponsor.name,
                    payOverhang = sponsor.payOverhang,
                    payExact = sponsor.payExact
                ),
                institution = InstitutionResponse(
                    id = institution.id ?: 0,
                    name = institution.name,
                    email = institution.email,
                    phonenumber = institution.phonenumber
                ),
                hourMode = entity.hourMode,
                hourCorridor = entity.hourCorridor?.let { AssistancePlanHourCorridorSoloDto.of(it) },
                hours = entity.hours
                    .sortedBy { it.id }
                    .map { AssistancePlanHourSoloDto.of(it, entity) },
                goals = entity.goals
                    .sortedBy { it.id }
                    .map { AssistancePlanGoalSoloDto.of(it, entity.id) }
            )
        }
    }
}

data class AssistancePlanSponsorSoloDto(
    val id: Long,
    val name: String,
    val payOverhang: Boolean,
    val payExact: Boolean
)

data class AssistancePlanHourCorridorSoloDto(
    val id: Long,
    val title: String,
    val weeklyMinutesFrom: Int,
    val weeklyMinutesTill: Int,
    val hourTypeId: Long,
    val hourTypeTitle: String
) {
    companion object {
        fun of(entity: HourCorridor): AssistancePlanHourCorridorSoloDto {
            return AssistancePlanHourCorridorSoloDto(
                id = entity.id,
                title = entity.title,
                weeklyMinutesFrom = entity.weeklyMinutesFrom,
                weeklyMinutesTill = entity.weeklyMinutesTill,
                hourTypeId = entity.hourType?.id ?: 0,
                hourTypeTitle = entity.hourType?.title ?: ""
            )
        }
    }
}

data class AssistancePlanHourTypeSoloDto(
    val id: Long,
    val title: String,
    val price: Double
) {
    companion object {
        fun of(entity: HourType?): AssistancePlanHourTypeSoloDto {
            return AssistancePlanHourTypeSoloDto(
                id = entity?.id ?: 0,
                title = entity?.title ?: "",
                price = entity?.price ?: 0.0
            )
        }
    }
}

data class AssistancePlanReferenceDto(
    val id: Long,
    val start: LocalDate,
    val end: LocalDate
)

data class AssistancePlanHourSoloDto(
    val id: Long,
    val weeklyMinutes: Int,
    val hourType: AssistancePlanHourTypeSoloDto,
    val assistancePlan: AssistancePlanReferenceDto
) {
    companion object {
        fun of(entity: AssistancePlanHour, plan: AssistancePlan): AssistancePlanHourSoloDto {
            return AssistancePlanHourSoloDto(
                id = entity.id,
                weeklyMinutes = entity.weeklyMinutes,
                hourType = AssistancePlanHourTypeSoloDto.of(entity.hourType),
                assistancePlan = AssistancePlanReferenceDto(plan.id, plan.start, plan.end)
            )
        }
    }
}

data class AssistancePlanGoalSoloDto(
    val id: Long,
    val title: String,
    val description: String,
    val assistancePlanId: Long,
    val hours: List<AssistancePlanGoalHourSoloDto>
) {
    companion object {
        fun of(entity: Goal, assistancePlanId: Long): AssistancePlanGoalSoloDto {
            return AssistancePlanGoalSoloDto(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                assistancePlanId = assistancePlanId,
                hours = entity.hours
                    .sortedBy { it.id }
                    .map { AssistancePlanGoalHourSoloDto.of(it) }
            )
        }
    }
}

data class AssistancePlanGoalHourSoloDto(
    val id: Long,
    val weeklyMinutes: Int,
    val hourType: AssistancePlanHourTypeSoloDto
) {
    companion object {
        fun of(entity: GoalHour): AssistancePlanGoalHourSoloDto {
            return AssistancePlanGoalHourSoloDto(
                id = entity.id,
                weeklyMinutes = entity.weeklyMinutes,
                hourType = AssistancePlanHourTypeSoloDto.of(entity.hourType)
            )
        }
    }
}
