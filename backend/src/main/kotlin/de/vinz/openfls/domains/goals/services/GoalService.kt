package de.vinz.openfls.domains.goals.services

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.goals.dtos.GoalDto
import de.vinz.openfls.domains.goals.dtos.GoalHourDto
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.goals.repositories.GoalHourRepository
import de.vinz.openfls.domains.goals.repositories.GoalRepository
import de.vinz.openfls.services.GenericService
import de.vinz.openfls.services.HourTypeService
import de.vinz.openfls.services.InstitutionService
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
): GenericService<Goal> {

    fun create(valueDto: GoalDto): GoalDto {
        val entity = modelMapper.map(valueDto, Goal::class.java)

        entity.assistancePlan = assistancePlanService.getById(valueDto.assistancePlanId)
                ?: throw IllegalArgumentException("assistance plan [id = ${valueDto.assistancePlanId}] not found")

        if (valueDto.institutionId != null) {
            entity.institution = institutionService.getById(valueDto.institutionId!!)
                    ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        }

        entity.hours = valueDto.hours
                .map { modelMapper.map(it, GoalHour::class.java).apply {
                    hourType = hourTypeService.getById(it.hourTypeId)
                            ?: throw IllegalArgumentException("hour type with id ${hourType?.id ?: 0} not found")
                } }
                .toMutableSet()

        val savedEntity = create(entity)

        valueDto.apply {
            id = savedEntity.id
            hours = savedEntity.hours
                    .map { modelMapper.map(it, GoalHourDto::class.java) }
                    .toMutableSet()
        }

        return valueDto
    }

    override fun create(value: Goal): Goal {
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

    fun update(valueDto: GoalDto): GoalDto {
        val entity = modelMapper.map(valueDto, Goal::class.java)

        entity.assistancePlan = assistancePlanService.getById(valueDto.assistancePlanId)
                ?: throw IllegalArgumentException("assistance plan [id = ${valueDto.assistancePlanId}] not found")

        if (valueDto.institutionId != null) {
            entity.institution = institutionService.getById(valueDto.institutionId!!)
                    ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
        }

        entity.hours = valueDto.hours
                .map { modelMapper.map(it, GoalHour::class.java).apply {
                    hourType = hourTypeService.getById(it.hourTypeId)
                            ?: throw IllegalArgumentException("hour type with id ${hourType?.id ?: 0} not found")
                } }
                .toMutableSet()

        val savedEntity = update(entity)

        valueDto.apply {
            this.id = savedEntity.id
            hours = savedEntity.hours
                    .map { modelMapper.map(it, GoalHourDto::class.java) }
                    .toMutableSet()
        }

        return valueDto
    }

    override fun update(value: Goal): Goal {
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

    override fun delete(id: Long) {
        if (id <= 0)
            throw IllegalArgumentException("id is not set")
        if (!goalRepository.existsById(id))
            throw IllegalArgumentException("id not found")

        goalRepository.deleteById(id)
    }

    override fun getAll(): List<Goal> {
        return goalRepository.findAll().toList()
    }

    fun getDtoById(id: Long): GoalDto? {
        return modelMapper.map(getById(id), GoalDto::class.java)
    }

    override fun getById(id: Long): Goal? {
        return goalRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return goalRepository.existsById(id)
    }

    fun getByAssistancePlanId(id: Long): List<GoalDto> {
        val entities = goalRepository.findByAssistancePlanId(id)

        return entities.map {
            modelMapper.map(it, GoalDto::class.java)
        }
    }
}