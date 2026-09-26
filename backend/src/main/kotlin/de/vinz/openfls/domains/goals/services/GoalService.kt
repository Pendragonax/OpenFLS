package de.vinz.openfls.domains.goals.services

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode
import de.vinz.openfls.domains.goals.dtos.GoalCreateDto
import de.vinz.openfls.domains.goals.dtos.GoalDto
import de.vinz.openfls.domains.goals.dtos.GoalUpdateDto
import de.vinz.openfls.domains.goals.dtos.GoalWithHours
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.goals.repositories.GoalHourRepository
import de.vinz.openfls.domains.goals.repositories.GoalRepository

import de.vinz.openfls.domains.hourTypes.HourTypeService
import de.vinz.openfls.domains.institutions.InstitutionService
import org.springframework.transaction.annotation.Transactional
import org.modelmapper.ModelMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GoalService(
        private val goalRepository: GoalRepository,
        private val goalHourRepository: GoalHourRepository,
        private val assistancePlanService: AssistancePlanService,
        private val institutionService: InstitutionService,
        private val hourTypeService: HourTypeService,
        private val modelMapper: ModelMapper
) {

    @Transactional
    fun create(valueDto: GoalCreateDto): GoalWithHours {
        val entity = Goal(title = valueDto.title, description = valueDto.description)

        entity.assistancePlan = assistancePlanService.getEntityById(valueDto.assistancePlanId)
                ?: throw IllegalArgumentException("assistance plan [id = ${valueDto.assistancePlanId}] not found")
        validateGoalHoursForAssistancePlan(entity.assistancePlan!!, valueDto.hours.isNotEmpty())

        if (valueDto.institutionId != null) {
            entity.institution = institutionService.getEntityById(valueDto.institutionId!!)
                    ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        }

        entity.hours = valueDto.hours
                .map { modelMapper.map(it, GoalHour::class.java).apply {
                    hourType = hourTypeService.getEntityById(it.hourTypeId)
                            ?: throw IllegalArgumentException("hour type with id ${hourType?.id ?: 0} not found")
                } }
                .toMutableSet()

        val savedEntity = createEntity(entity)

        return toGoalWithHours(savedEntity)
    }

    @Transactional
    fun createEntity(value: Goal): Goal {
        if (value.id > 0)
            throw IllegalArgumentException("id is set")

        // backup hours
        val hours = value.hours
        value.hours = mutableSetOf()

        // save
        val entity = goalRepository.save(value)

        // add hours
        entity.hours = hours
            .map { goalHourRepository
                .save(it.apply {
                    id = 0
                    goal = entity
                })}
            .toMutableSet()

        return entity
    }

    @Transactional
    fun update(valueDto: GoalUpdateDto): GoalWithHours {
        val entity = modelMapper.map(valueDto, Goal::class.java)

        entity.assistancePlan = assistancePlanService.getEntityById(valueDto.assistancePlanId)
                ?: throw IllegalArgumentException("assistance plan [id = ${valueDto.assistancePlanId}] not found")
        validateGoalHoursForAssistancePlan(entity.assistancePlan!!, valueDto.hours.isNotEmpty())

        if (valueDto.institutionId != null) {
            entity.institution = institutionService.getEntityById(valueDto.institutionId!!)
                    ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        }

        entity.hours = valueDto.hours
                .map { modelMapper.map(it, GoalHour::class.java).apply {
                    hourType = hourTypeService.getEntityById(it.hourTypeId)
                            ?: throw IllegalArgumentException("hour type with id ${hourType?.id ?: 0} not found")
                } }
                .toMutableSet()

        val savedEntity = updateEntity(entity)

        return toGoalWithHours(savedEntity)
    }

    @Transactional
    fun updateEntity(value: Goal): Goal {
        if (value.id <= 0)
            throw IllegalArgumentException("id is not set")
        if (!goalRepository.existsById(value.id))
            throw IllegalArgumentException("id not found")

        // Backup goal hours
        val goalHours = value.hours
        value.hours = mutableSetOf()

        val entity = goalRepository.save(value)

        // delete goal hours
        goalHourRepository
            .findByGoalId(value.id)
            .filter { !goalHours.any { hour -> hour.id == it.id } }
            .forEach { goalHourRepository.deleteById(it.id) }

        // add / update goal hours
        entity.hours = goalHours
            .map { hour ->
                goalHourRepository.save(hour.apply {
                    goal = entity
                })}
            .toMutableSet()

        return entity
    }

    @Transactional
    fun delete(id: Long) {
        if (id <= 0)
            throw IllegalArgumentException("id is not set")
        if (!goalRepository.existsById(id))
            throw IllegalArgumentException("id not found")

        goalRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): GoalDto? {
        return modelMapper.map(getEntityById(id), GoalDto::class.java)
    }

    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Goal? {
        return goalRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return goalRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun getByAssistancePlanId(id: Long): List<GoalWithHours> {
        val entities = goalRepository.findByAssistancePlanId(id)

        return entities.map { toGoalWithHours(it) }
    }

    private fun toGoalWithHours(entity: Goal): GoalWithHours {
        return modelMapper.map(entity, GoalWithHours::class.java)
    }

    private fun validateGoalHoursForAssistancePlan(assistancePlan: de.vinz.openfls.domains.assistancePlans.AssistancePlan, hasGoalHours: Boolean) {
        if (assistancePlan.hourMode == AssistancePlanHourMode.CORRIDOR && hasGoalHours) {
            throw IllegalArgumentException("corridor assistance plans must not contain goal hours")
        }
    }
}
