package de.vinz.openfls.domains.goals.service

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.goals.dto.GoalCreateRequest
import de.vinz.openfls.domains.goals.dto.GoalCreateResult
import de.vinz.openfls.domains.goals.dto.GoalDeleteResult
import de.vinz.openfls.domains.goals.dto.GoalHourRequest
import de.vinz.openfls.domains.goals.dto.GoalResponse
import de.vinz.openfls.domains.goals.dto.GoalUpdateRequest
import de.vinz.openfls.domains.goals.dto.GoalUpdateResult
import de.vinz.openfls.domains.goals.dto.GoalWithHoursResponse
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.entity.GoalHour
import de.vinz.openfls.domains.goals.repository.GoalHourRepository
import de.vinz.openfls.domains.goals.repository.GoalRepository
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.domains.institutions.service.InstitutionService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GoalService(
    private val goalRepository: GoalRepository,
    private val goalHourRepository: GoalHourRepository,
    private val assistancePlanService: AssistancePlanService,
    private val institutionService: InstitutionService,
    private val hourTypeService: HourTypeService
) {

    @Transactional
    fun create(request: GoalCreateRequest): GoalCreateResult {
        val assistancePlan = assistancePlanService.getEntityById(request.assistancePlanId)
            ?: return GoalCreateResult.AssistancePlanNotFound(
                "assistance plan [id = ${request.assistancePlanId}] not found"
            )
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR && request.hours.isNotEmpty()) {
            return GoalCreateResult.CorridorHoursNotAllowed("corridor assistance plans must not contain goal hours")
        }
        val institution = request.institutionId?.let { institutionId ->
            institutionService.getEntityById(institutionId)
                ?: return GoalCreateResult.InstitutionNotFound("institution [id = $institutionId] not found")
        }
        val hourTypesById = mutableMapOf<Long, HourType>()
        for (hourRequest in request.hours) {
            if (hourRequest.hourTypeId !in hourTypesById) {
                val hourType = hourTypeService.getEntityById(hourRequest.hourTypeId)
                    ?: return GoalCreateResult.HourTypeNotFound("hour type with id ${hourRequest.hourTypeId} not found")
                hourTypesById[hourRequest.hourTypeId] = hourType
            }
        }

        val entity = Goal(
            title = request.title,
            description = request.description,
            assistancePlan = assistancePlan,
            institution = institution
        )
        val saved = goalRepository.save(entity)
        saved.hours = request.hours
            .map { hourRequest -> goalHourRepository.save(buildGoalHour(hourRequest, hourTypesById, saved)) }
            .toMutableSet()

        return GoalCreateResult.Success(GoalWithHoursResponse.from(saved))
    }

    @Transactional
    fun update(request: GoalUpdateRequest): GoalUpdateResult {
        val entity = goalRepository.findByIdOrNull(request.id)
            ?: return GoalUpdateResult.NotFound
        val assistancePlan = assistancePlanService.getEntityById(request.assistancePlanId)
            ?: return GoalUpdateResult.AssistancePlanNotFound(
                "assistance plan [id = ${request.assistancePlanId}] not found"
            )
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR && request.hours.isNotEmpty()) {
            return GoalUpdateResult.CorridorHoursNotAllowed("corridor assistance plans must not contain goal hours")
        }
        val institution = request.institutionId?.let { institutionId ->
            institutionService.getEntityById(institutionId)
                ?: return GoalUpdateResult.InstitutionNotFound("institution [id = $institutionId] not found")
        }
        val hourTypesById = mutableMapOf<Long, HourType>()
        for (hourRequest in request.hours) {
            if (hourRequest.hourTypeId !in hourTypesById) {
                val hourType = hourTypeService.getEntityById(hourRequest.hourTypeId)
                    ?: return GoalUpdateResult.HourTypeNotFound("hour type with id ${hourRequest.hourTypeId} not found")
                hourTypesById[hourRequest.hourTypeId] = hourType
            }
        }
        val existingHourIds = entity.hours.map { it.id }.toSet()
        val foreignHourId = request.hours.firstOrNull { it.id > 0 && it.id !in existingHourIds }?.id
        if (foreignHourId != null) {
            return GoalUpdateResult.HourNotInGoal("goal hour with id $foreignHourId does not belong to goal ${entity.id}")
        }

        entity.title = request.title
        entity.description = request.description
        entity.assistancePlan = assistancePlan
        entity.institution = institution
        val saved = goalRepository.save(entity)

        val requestedHourIds = request.hours.filter { it.id > 0 }.map { it.id }.toSet()
        goalHourRepository.findByGoalId(saved.id)
            .filter { it.id !in requestedHourIds }
            .forEach { goalHourRepository.deleteById(it.id) }

        saved.hours = request.hours
            .map { hourRequest -> goalHourRepository.save(buildGoalHour(hourRequest, hourTypesById, saved)) }
            .toMutableSet()

        return GoalUpdateResult.Success(GoalWithHoursResponse.from(saved))
    }

    @Transactional
    fun delete(id: Long): GoalDeleteResult {
        val entity = goalRepository.findByIdOrNull(id)
            ?: return GoalDeleteResult.NotFound
        val response = GoalResponse.from(entity)
        goalRepository.deleteById(id)
        return GoalDeleteResult.Success(response)
    }

    @Transactional(readOnly = true)
    fun getByAssistancePlanId(id: Long): List<GoalWithHoursResponse> {
        return goalRepository.findByAssistancePlanId(id).map { GoalWithHoursResponse.from(it) }
    }

    private fun buildGoalHour(hourRequest: GoalHourRequest, hourTypesById: Map<Long, HourType>, goal: Goal): GoalHour {
        val hour = if (hourRequest.id > 0) GoalHour(id = hourRequest.id) else GoalHour()
        hour.weeklyMinutes = hourRequest.weeklyMinutes
        hour.hourType = hourTypesById.getValue(hourRequest.hourTypeId)
        hour.goal = goal
        return hour
    }
}
