package de.vinz.openfls.domains.evaluations.repository

import de.vinz.openfls.domains.evaluations.entity.Evaluation
import org.springframework.data.repository.CrudRepository

interface EvaluationRepository : CrudRepository<Evaluation, Long> {

    fun findAllByGoalIdIn(ids: List<Long>): List<Evaluation>
}
