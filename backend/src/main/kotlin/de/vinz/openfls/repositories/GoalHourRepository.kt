package de.vinz.openfls.repositories

import de.vinz.openfls.entities.GoalHour
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface GoalHourRepository : CrudRepository<GoalHour, Long> {

    @Query("SELECT u FROM GoalHour u WHERE u.goal.id = :goalId")
    fun findByGoalId(@Param("goalId") assistancePlanId: Long): List<GoalHour>
}