package de.vinz.openfls.repositories

import de.vinz.openfls.model.AssistancePlan
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AssistancePlanRepository: CrudRepository<AssistancePlan, Long> {
    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findAllByYear(@Param("year") year: Int): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.client.id = :clientId")
    fun findByClientId(@Param("clientId") id: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.sponsor.id = :sponsorId")
    fun findBySponsorId(@Param("sponsorId") id: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.sponsor.id = :sponsorId" +
            " AND u.start <= :date AND u.end >= :date")
    fun findBySponsorIdAndDate(@Param("sponsorId") id: Long,
                               @Param("date") date: LocalDate): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.sponsor.id = :sponsorId" +
            " AND (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findBySponsorIdAndYear(@Param("sponsorId") id: Long,
                               @Param("year") year: Int): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId")
    fun findByInstitutionId(@Param("institutionId") id: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId" +
            " AND u.start <= :date AND u.end >= :date")
    fun findByInstitutionIdAndDate(@Param("institutionId") id: Long,
                                   @Param("date") date: LocalDate): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId" +
            " AND (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findByInstitutionIdAndYear(@Param("institutionId") id: Long,
                                   @Param("year") year: Int): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId AND u.sponsor.id = :sponsorId")
    fun findByInstitutionIdAndSponsorId(@Param("institutionId") institutionId: Long,
                                        @Param("sponsorId") sponsorId: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId AND u.sponsor.id = :sponsorId" +
            " AND u.start <= :date AND u.end >= :date")
    fun findByInstitutionIdAndSponsorIdAndDate(@Param("institutionId") institutionId: Long,
                                               @Param("sponsorId") sponsorId: Long,
                                               @Param("date") date: LocalDate): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId AND u.sponsor.id = :sponsorId" +
            " AND (YEAR(u.start) <= :year AND YEAR(u.end) >= :year)")
    fun findByInstitutionIdAndSponsorIdAndYear(@Param("institutionId") institutionId: Long,
                                               @Param("sponsorId") sponsorId: Long,
                                               @Param("year") year: Int): List<AssistancePlan>
}