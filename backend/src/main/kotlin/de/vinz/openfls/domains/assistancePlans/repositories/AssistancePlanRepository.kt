package de.vinz.openfls.domains.assistancePlans.repositories

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AssistancePlanRepository: CrudRepository<AssistancePlan, Long> {

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE :end >= u.start AND :start <= u.end")
    fun findProjectionByStartAndEnd(start: LocalDate,
                                    end: LocalDate): List<AssistancePlanProjection>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId " +
            "AND (:end >= u.start AND :start <= u.end)")
    fun findProjectionByInstitutionIdAndStartAndEnd(institutionId: Long,
                                                    start: LocalDate,
                                                    end: LocalDate): List<AssistancePlanProjection>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId " +
            "And u.sponsor.id = :sponsorId " +
            "AND (:end >= u.start AND :start <= u.end)")
    fun findProjectionByInstitutionIdAndSponsorIdAndStartAndEnd(institutionId: Long,
                                                                sponsorId: Long,
                                                                start: LocalDate,
                                                                end: LocalDate): List<AssistancePlanProjection>

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
            "WHERE u.institution.id = :institutionId")
    fun findProjectionsByInstitutionId(@Param("institutionId") id: Long): List<AssistancePlanProjection>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.institution.id = :institutionId")
    fun findProjectionByInstitutionId(@Param("institutionId") institutionId: Long): List<AssistancePlanProjection>

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