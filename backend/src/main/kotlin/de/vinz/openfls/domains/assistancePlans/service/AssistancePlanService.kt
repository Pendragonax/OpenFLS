package de.vinz.openfls.domains.assistancePlans.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanCreateResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanDeleteResult
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanDetailResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanEditResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanResponse
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateRequest
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanUpdateResult
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.repository.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.entity.GoalHour
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.service.HourCorridorService
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.sponsors.service.SponsorService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssistancePlanService(
    private val assistancePlanRepository: AssistancePlanRepository,
    private val hourCorridorService: HourCorridorService,
    private val clientService: ClientService,
    private val institutionService: InstitutionService,
    private val sponsorService: SponsorService,
    private val hourTypeService: HourTypeService
) {

    @Transactional
    fun create(request: AssistancePlanCreateRequest): AssistancePlanCreateResult {
        getCreateHoursViolation(request)?.let { return AssistancePlanCreateResult.InvalidHours(it) }

        val hourCorridor = if (request.hourMode == AssistancePlanHourMode.CORRIDOR) {
            hourCorridorService.getEntityById(request.hourCorridorId)
                ?: return AssistancePlanCreateResult.HourCorridorNotFound
        } else {
            null
        }
        val client = clientService.getEntityById(request.clientId)
            ?: return AssistancePlanCreateResult.ClientNotFound
        if (client.archived) {
            return AssistancePlanCreateResult.ClientArchived
        }
        val institution = institutionService.getEntityById(request.institutionId)
            ?: return AssistancePlanCreateResult.InstitutionNotFound
        val sponsor = sponsorService.getEntityById(request.sponsorId)
            ?: return AssistancePlanCreateResult.SponsorNotFound
        val hourTypes = loadHourTypes(
            request.hours.map { it.hourTypeId } + request.goals.flatMap { goal -> goal.hours.map { it.hourTypeId } }
        ) ?: return AssistancePlanCreateResult.HourTypeNotFound
        val goalInstitutions = loadInstitutions(request.goals.mapNotNull { it.institutionId })
            ?: return AssistancePlanCreateResult.InstitutionNotFound

        val assistancePlan = AssistancePlan(
            start = request.start,
            end = request.end,
            hourMode = request.hourMode,
            hourCorridor = hourCorridor,
            client = client,
            institution = institution,
            sponsor = sponsor
        )
        request.hours.forEach { hour ->
            assistancePlan.hours.add(
                AssistancePlanHour(
                    weeklyMinutes = hour.weeklyMinutes,
                    hourType = hourTypes.getValue(hour.hourTypeId),
                    assistancePlan = assistancePlan
                )
            )
        }
        request.goals.forEach { goalRequest ->
            val goal = Goal(
                title = goalRequest.title,
                description = goalRequest.description,
                institution = goalRequest.institutionId?.let { goalInstitutions.getValue(it) },
                assistancePlan = assistancePlan
            )
            goalRequest.hours.forEach { hour ->
                goal.hours.add(
                    GoalHour(weeklyMinutes = hour.weeklyMinutes, hourType = hourTypes.getValue(hour.hourTypeId), goal = goal)
                )
            }
            assistancePlan.goals.add(goal)
        }

        return AssistancePlanCreateResult.Success(
            AssistancePlanResponse.from(assistancePlanRepository.save(assistancePlan))
        )
    }

    @Transactional
    fun update(id: Long, request: AssistancePlanUpdateRequest): AssistancePlanUpdateResult {
        val assistancePlan = assistancePlanRepository.findByIdOrNull(id)
            ?: return AssistancePlanUpdateResult.NotFound
        if (assistancePlan.hourMode != request.hourMode) {
            return AssistancePlanUpdateResult.HourModeChanged
        }
        getUpdateHoursViolation(assistancePlan, request)?.let { return AssistancePlanUpdateResult.InvalidHours(it) }

        val hourCorridor = if (request.hourMode == AssistancePlanHourMode.CORRIDOR) {
            hourCorridorService.getEntityById(request.hourCorridorId)
                ?: return AssistancePlanUpdateResult.HourCorridorNotFound
        } else {
            null
        }
        val client = clientService.getEntityById(request.clientId)
            ?: return AssistancePlanUpdateResult.ClientNotFound
        if (client.archived) {
            return AssistancePlanUpdateResult.ClientArchived
        }
        val institution = institutionService.getEntityById(request.institutionId)
            ?: return AssistancePlanUpdateResult.InstitutionNotFound
        val sponsor = sponsorService.getEntityById(request.sponsorId)
            ?: return AssistancePlanUpdateResult.SponsorNotFound
        val hourTypes = loadHourTypes(
            request.hours.map { it.hourTypeId } + request.goals.flatMap { goal -> goal.hours.map { it.hourTypeId } }
        ) ?: return AssistancePlanUpdateResult.HourTypeNotFound
        val goalInstitutions = loadInstitutions(request.goals.mapNotNull { it.institutionId })
            ?: return AssistancePlanUpdateResult.InstitutionNotFound

        assistancePlan.start = request.start
        assistancePlan.end = request.end
        assistancePlan.hourCorridor = hourCorridor
        assistancePlan.client = client
        assistancePlan.institution = institution
        assistancePlan.sponsor = sponsor

        syncHours(assistancePlan, request, hourTypes)
        syncGoals(assistancePlan, request, hourTypes, goalInstitutions)

        return AssistancePlanUpdateResult.Success(
            AssistancePlanResponse.from(assistancePlanRepository.save(assistancePlan))
        )
    }

    @Transactional
    fun delete(id: Long): AssistancePlanDeleteResult {
        val assistancePlan = assistancePlanRepository.findByIdOrNull(id)
            ?: return AssistancePlanDeleteResult.NotFound
        val response = AssistancePlanResponse.from(assistancePlan)
        assistancePlanRepository.delete(assistancePlan)

        return AssistancePlanDeleteResult.Success(response)
    }

    @Transactional(readOnly = true)
    fun getEditById(
        id: Long,
        includeArchived: Boolean,
        leadingInstitutionIds: List<Long>
    ): AssistancePlanEditResponse? {
        val assistancePlan = assistancePlanRepository.findByIdOrNull(id) ?: return null

        return if (isVisible(assistancePlan, includeArchived, leadingInstitutionIds)) {
            AssistancePlanEditResponse.from(assistancePlan)
        } else {
            null
        }
    }

    @Transactional(readOnly = true)
    fun getDetailById(id: Long): AssistancePlanDetailResponse? {
        return assistancePlanRepository.findDetailedById(id)?.let { AssistancePlanDetailResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getAllEditResponsesByYearAndInstitutionIdAndSponsorId(
        year: Int,
        institutionId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanEditResponse> {
        val assistancePlans = when {
            institutionId != null && sponsorId != null ->
                assistancePlanRepository.findByInstitutionIdAndSponsorIdAndYear(institutionId, sponsorId, year)

            institutionId != null ->
                assistancePlanRepository.findByInstitutionIdAndYear(institutionId, year)

            sponsorId != null ->
                assistancePlanRepository.findBySponsorIdAndYear(sponsorId, year)

            else ->
                assistancePlanRepository.findAllByYear(year)
        }

        return assistancePlans.map { AssistancePlanEditResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): AssistancePlan? {
        return assistancePlanRepository.findByIdOrNull(id)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByClientId(clientId: Long): List<AssistancePlan> {
        return assistancePlanRepository.findByClientId(clientId)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return assistancePlanRepository.existsById(id)
    }

    private fun syncHours(
        assistancePlan: AssistancePlan,
        request: AssistancePlanUpdateRequest,
        hourTypes: Map<Long, HourType>
    ) {
        val existingHours = assistancePlan.hours.associateBy { it.id }
        val keptIds = request.hours.map { it.id }.filter { it in existingHours }.toSet()
        assistancePlan.hours.removeIf { it.id !in keptIds }

        request.hours.forEach { hourRequest ->
            val hourType = hourTypes.getValue(hourRequest.hourTypeId)
            val existing = existingHours[hourRequest.id]
            if (existing != null) {
                existing.weeklyMinutes = hourRequest.weeklyMinutes
                existing.hourType = hourType
            } else {
                assistancePlan.hours.add(
                    AssistancePlanHour(
                        weeklyMinutes = hourRequest.weeklyMinutes,
                        hourType = hourType,
                        assistancePlan = assistancePlan
                    )
                )
            }
        }
    }

    private fun syncGoals(
        assistancePlan: AssistancePlan,
        request: AssistancePlanUpdateRequest,
        hourTypes: Map<Long, HourType>,
        goalInstitutions: Map<Long, Institution>
    ) {
        val existingGoals = assistancePlan.goals.associateBy { it.id }
        val keptIds = request.goals.map { it.id }.filter { it in existingGoals }.toSet()
        assistancePlan.goals.removeIf { it.id !in keptIds }

        request.goals.forEach { goalRequest ->
            val goal = existingGoals[goalRequest.id]
                ?: Goal(assistancePlan = assistancePlan).also { assistancePlan.goals.add(it) }
            goal.title = goalRequest.title
            goal.description = goalRequest.description
            goal.institution = goalRequest.institutionId?.let { goalInstitutions.getValue(it) }

            val existingHours = goal.hours.associateBy { it.id }
            val keptHourIds = goalRequest.hours.map { it.id }.filter { it in existingHours }.toSet()
            goal.hours.removeIf { it.id !in keptHourIds }
            goalRequest.hours.forEach { hourRequest ->
                val hourType = hourTypes.getValue(hourRequest.hourTypeId)
                val existing = existingHours[hourRequest.id]
                if (existing != null) {
                    existing.weeklyMinutes = hourRequest.weeklyMinutes
                    existing.hourType = hourType
                } else {
                    goal.hours.add(GoalHour(weeklyMinutes = hourRequest.weeklyMinutes, hourType = hourType, goal = goal))
                }
            }
        }
    }

    private fun loadHourTypes(hourTypeIds: List<Long>): Map<Long, HourType>? {
        return hourTypeIds.distinct().associateWith { hourTypeService.getEntityById(it) ?: return null }
    }

    private fun loadInstitutions(institutionIds: List<Long>): Map<Long, Institution>? {
        return institutionIds.distinct().associateWith { institutionService.getEntityById(it) ?: return null }
    }

    private fun getCreateHoursViolation(request: AssistancePlanCreateRequest): String? {
        val hasPlanHours = request.hours.isNotEmpty()
        val hasGoalHours = request.goals.any { it.hours.isNotEmpty() }

        if (request.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return getCorridorViolation(request.hourCorridorId, hasPlanHours, hasGoalHours)
        }
        if (hasPlanHours && hasGoalHours) {
            return BOTH_HOURS_MESSAGE
        }
        if (request.hourCorridorId > 0) {
            return EXACT_WITH_CORRIDOR_MESSAGE
        }

        return null
    }

    private fun getUpdateHoursViolation(assistancePlan: AssistancePlan, request: AssistancePlanUpdateRequest): String? {
        val hasPlanHours = request.hours.isNotEmpty()
        val hasGoalHours = request.goals.any { it.hours.isNotEmpty() }

        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR) {
            return getCorridorViolation(request.hourCorridorId, hasPlanHours, hasGoalHours)
        }
        if (!hasPlanHours || !hasGoalHours) {
            return if (request.hourCorridorId > 0) EXACT_WITH_CORRIDOR_MESSAGE else null
        }

        // hours in both areas are only tolerated for plans that already have them, and only to shrink them
        val existingHasPlanHours = assistancePlan.hours.isNotEmpty()
        val existingHasGoalHours = assistancePlan.goals.any { it.hours.isNotEmpty() }
        if (!existingHasPlanHours || !existingHasGoalHours) {
            return BOTH_HOURS_MESSAGE
        }

        val existingPlanHourIds = assistancePlan.hours.map { it.id }.toSet()
        val existingGoalHourIds = assistancePlan.goals.flatMap { goal -> goal.hours.map { it.id } }.toSet()
        val hasNewPlanHours = request.hours.any { it.id <= 0 || it.id !in existingPlanHourIds }
        val hasNewGoalHours = request.goals
            .flatMap { it.hours }
            .any { it.id <= 0 || it.id !in existingGoalHourIds }
        if (hasNewPlanHours || hasNewGoalHours) {
            return NEW_HOURS_IN_BOTH_AREAS_MESSAGE
        }

        return if (request.hourCorridorId > 0) EXACT_WITH_CORRIDOR_MESSAGE else null
    }

    private fun getCorridorViolation(hourCorridorId: Long, hasPlanHours: Boolean, hasGoalHours: Boolean): String? {
        if (hourCorridorId <= 0) {
            return "corridor assistance plans require an hour corridor"
        }
        if (hasPlanHours) {
            return "corridor assistance plans must not contain plan hours"
        }
        if (hasGoalHours) {
            return "corridor assistance plans must not contain goal hours"
        }

        return null
    }

    private fun isVisible(
        assistancePlan: AssistancePlan,
        includeArchived: Boolean,
        leadingInstitutionIds: List<Long>
    ): Boolean {
        val archived = assistancePlan.client?.archived == true
        val institutionId = assistancePlan.client?.institution?.id ?: 0

        return !archived || includeArchived || leadingInstitutionIds.contains(institutionId)
    }

    private companion object {
        const val BOTH_HOURS_MESSAGE =
            "Stunden dürfen entweder direkt im Hilfeplan oder in den Zielen hinterlegt sein, nicht in beiden Bereichen gleichzeitig."
        const val NEW_HOURS_IN_BOTH_AREAS_MESSAGE =
            "Bei Hilfeplänen mit Stunden in beiden Bereichen dürfen keine neuen Stunden hinzugefügt werden. Bitte erst bestehende Stunden löschen, bis nur noch ein Bereich Stunden enthält."
        const val EXACT_WITH_CORRIDOR_MESSAGE = "exact assistance plans must not reference an hour corridor"
    }
}
