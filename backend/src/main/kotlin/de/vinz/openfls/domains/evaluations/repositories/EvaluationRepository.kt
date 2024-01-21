package de.vinz.openfls.domains.evaluations.repositories

import de.vinz.openfls.domains.evaluations.entities.Evaluation
import org.springframework.data.repository.CrudRepository

interface EvaluationRepository: CrudRepository<Evaluation, Long> {

    fun findAllByGoalIdIn(ids: List<Long>): List<Evaluation>

    fun findAllByGoalId(id: Long): List<Evaluation>

}