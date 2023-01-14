package de.vinz.openfls.repositories

import de.vinz.openfls.model.AssistancePlan
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface AssistancePlanRepository: CrudRepository<AssistancePlan, Long> {
    @Query("SELECT u FROM AssistancePlan u WHERE u.client.id = :clientId")
    fun findByClientId(@Param("clientId") id: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u WHERE u.sponsor.id = :sponsorId")
    fun findBySponsorId(@Param("sponsorId") id: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u WHERE u.institution.id = :institutionId")
    fun findByInstitutionId(@Param("institutionId") id: Long): List<AssistancePlan>
}