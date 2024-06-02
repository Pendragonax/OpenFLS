package de.vinz.openfls.domains.evaluations

import de.vinz.openfls.domains.evaluations.Evaluation
import org.springframework.data.repository.CrudRepository

interface EvaluationRepository: CrudRepository<Evaluation, Long> {

    fun findAllByGoalIdIn(ids: List<Long>): List<Evaluation>

    fun findAllByGoalId(id: Long): List<Evaluation>

}