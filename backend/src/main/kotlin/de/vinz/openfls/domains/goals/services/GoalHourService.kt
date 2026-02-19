package de.vinz.openfls.domains.goals.services

import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.goals.repositories.GoalHourRepository
import de.vinz.openfls.services.GenericService
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class GoalHourService(
    private val goalHourRepository: GoalHourRepository
): GenericService<GoalHour> {

    @Transactional
    override fun create(value: GoalHour): GoalHour {
        return goalHourRepository.save(value)
    }

    @Transactional
    override fun update(value: GoalHour): GoalHour {
        if (!goalHourRepository.existsById(value.id))
            throw IllegalArgumentException("goal hour does not exists")

        return goalHourRepository.save(value)
    }

    @Transactional
    override fun delete(id: Long) {
        if (!goalHourRepository.existsById(id))
            throw IllegalArgumentException("goal hour does not exists")

        goalHourRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<GoalHour> {
        return goalHourRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    override fun getById(id: Long): GoalHour? {
        return goalHourRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    override fun existsById(id: Long): Boolean {
        return goalHourRepository.existsById(id)
    }
}
