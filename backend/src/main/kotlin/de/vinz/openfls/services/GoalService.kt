package de.vinz.openfls.services

import de.vinz.openfls.model.Goal
import de.vinz.openfls.repositories.GoalHourRepository
import de.vinz.openfls.repositories.GoalRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GoalService(
    private val goalRepository: GoalRepository,
    private val goalHourRepository: GoalHourRepository
): GenericService<Goal> {
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

    override fun getById(id: Long): Goal? {
        return goalRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return goalRepository.existsById(id)
    }

    fun getByAssistancePlanId(id: Long): MutableSet<Goal> {
        return goalRepository.findByAssistancePlanId(id).toMutableSet()
    }
}