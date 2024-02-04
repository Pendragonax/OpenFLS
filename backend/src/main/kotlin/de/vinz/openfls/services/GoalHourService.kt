package de.vinz.openfls.services

import de.vinz.openfls.entities.GoalHour
import de.vinz.openfls.repositories.GoalHourRepository
import org.springframework.stereotype.Service

@Service
class GoalHourService(
    private val goalHourRepository: GoalHourRepository
): GenericService<GoalHour> {
    override fun create(value: GoalHour): GoalHour {
        return goalHourRepository.save(value)
    }

    override fun update(value: GoalHour): GoalHour {
        if (!goalHourRepository.existsById(value.id))
            throw IllegalArgumentException("goal hour does not exists")

        return goalHourRepository.save(value)
    }

    override fun delete(id: Long) {
        if (!goalHourRepository.existsById(id))
            throw IllegalArgumentException("goal hour does not exists")

        goalHourRepository.deleteById(id)
    }

    override fun getAll(): List<GoalHour> {
        return goalHourRepository.findAll().toList()
    }

    override fun getById(id: Long): GoalHour? {
        return goalHourRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return goalHourRepository.existsById(id)
    }
}