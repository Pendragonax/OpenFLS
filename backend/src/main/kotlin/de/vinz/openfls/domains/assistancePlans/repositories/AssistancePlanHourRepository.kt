package de.vinz.openfls.domains.assistancePlans.repositories

import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanHourProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanHourSoloProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface AssistancePlanHourRepository : CrudRepository<AssistancePlanHour, Long> {

    @Query("SELECT u FROM AssistancePlanHour u WHERE u.assistancePlan.id = :assistancePlanId")
    fun findByAssistancePlanId(@Param("assistancePlanId") assistancePlanId: Long): List<AssistancePlanHour>

    @Query("SELECT a FROM AssistancePlanHour a WHERE a.id = :id")
    fun findProjectionById(@Param("id") id: Long): AssistancePlanHourSoloProjection
}