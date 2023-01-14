package de.vinz.openfls.repositories

import de.vinz.openfls.model.AssistancePlanHour
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface AssistancePlanHourRepository : CrudRepository<AssistancePlanHour, Long> {

    @Query("SELECT u FROM AssistancePlanHour u WHERE u.assistancePlan.id = :assistancePlanId")
    fun findByAssistancePlanId(@Param("assistancePlanId") assistancePlanId: Long): List<AssistancePlanHour>
}