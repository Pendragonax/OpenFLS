package de.vinz.openfls.domains.assistancePlans.repository

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface AssistancePlanRepository : CrudRepository<AssistancePlan, Long> {

    @Query(
        "SELECT DISTINCT ap from AssistancePlan ap " +
            "left join fetch ap.client " +
            "left join fetch ap.sponsor " +
            "left join fetch ap.institution " +
            "left join fetch ap.hourCorridor hc " +
            "left join fetch hc.hourType " +
            "left join fetch ap.goals g " +
            "left join fetch g.hours gh " +
            "left join fetch gh.hourType " +
            "left join fetch ap.hours aph " +
            "left join fetch aph.hourType " +
            "where ap.id=:id"
    )
    fun findDetailedById(id: Long): AssistancePlan?

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findAllByYear(@Param("year") year: Int): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.client.id = :clientId " +
            "ORDER BY u.start DESC")
    fun findByClientId(@Param("clientId") id: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.sponsor.id = :sponsorId" +
            " AND (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findBySponsorIdAndYear(@Param("sponsorId") id: Long,
                               @Param("year") year: Int): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId" +
            " AND (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findByInstitutionIdAndYear(@Param("institutionId") id: Long,
                                   @Param("year") year: Int): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId AND u.sponsor.id = :sponsorId" +
            " AND (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findByInstitutionIdAndSponsorIdAndYear(@Param("institutionId") institutionId: Long,
                                               @Param("sponsorId") sponsorId: Long,
                                               @Param("year") year: Int): List<AssistancePlan>
}
