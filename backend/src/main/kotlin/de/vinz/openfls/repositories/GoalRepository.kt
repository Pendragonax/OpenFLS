package de.vinz.openfls.repositories

import de.vinz.openfls.entities.Goal
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface GoalRepository: CrudRepository<Goal, Long> {
    @Query("SELECT u FROM Goal u WHERE u.assistancePlan.id = :assistancePlanId")
    fun findByAssistancePlanId(@Param("assistancePlanId") assistancePlanId: Long): List<Goal>
}