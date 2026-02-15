package de.vinz.openfls.domains.assistancePlans.repositories

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanPreviewProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanWeeklyMinutesProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AssistancePlanRepository: CrudRepository<AssistancePlan, Long> {

    @Query(
        "SELECT DISTINCT ap from AssistancePlan ap " +
            "left join fetch ap.goals g " +
            "left join fetch g.hours " +
            "left join fetch ap.hours " +
            "where ap.id=:id"
    )
    fun findProjectionById(id: Long): AssistancePlanProjection

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE :end >= u.start AND :start <= u.end")
    fun findProjectionByStartAndEnd(start: LocalDate,
                                    end: LocalDate): List<AssistancePlanProjection>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.sponsor.id = :sponsorId " +
            "AND (:end >= u.start AND :start <= u.end)")
    fun findProjectionBySponsorIdAndStartAndEnd(sponsorId: Long,
                                                start: LocalDate,
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
            "WHERE u.client.id = :clientId " +
            "ORDER BY u.start DESC")
    fun findByClientId(@Param("clientId") id: Long): List<AssistancePlan>

    @Query("SELECT u FROM AssistancePlan u " +
            "WHERE u.client.id = :clientId")
    fun findProjectionsByClientId(@Param("clientId") id: Long): List<AssistancePlanProjection>

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
            "WHERE u.sponsor.id = :sponsorId")
    fun findProjectionsBySponsorId(@Param("sponsorId") id: Long): List<AssistancePlanProjection>

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

    @Query(
        """
        SELECT ap.id as id,
               c.id as clientId,
               i.id as institutionId,
               s.id as sponsorId,
               c.firstName as clientFirstname,
               c.lastName as clientLastname,
               i.name as institutionName,
               s.name as sponsorName,
               ap.start as start,
               ap.end as end
        FROM AssistancePlan ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        WHERE ap.client.id = :clientId
        ORDER BY ap.start DESC
        """
    )
    fun findPreviewProjectionsByClientId(
        @Param("clientId") clientId: Long
    ): List<AssistancePlanPreviewProjection>

    @Query(
        """
        SELECT ap.id as id,
               c.id as clientId,
               i.id as institutionId,
               s.id as sponsorId,
               c.firstName as clientFirstname,
               c.lastName as clientLastname,
               i.name as institutionName,
               s.name as sponsorName,
               ap.start as start,
               ap.end as end
        FROM AssistancePlan ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        WHERE ap.institution.id = :institutionId
        ORDER BY ap.start DESC
        """
    )
    fun findPreviewProjectionsByInstitutionId(
        @Param("institutionId") institutionId: Long
    ): List<AssistancePlanPreviewProjection>

    @Query(
        """
        SELECT ap.id as id,
               c.id as clientId,
               i.id as institutionId,
               s.id as sponsorId,
               c.firstName as clientFirstname,
               c.lastName as clientLastname,
               i.name as institutionName,
               s.name as sponsorName,
               ap.start as start,
               ap.end as end
        FROM AssistancePlan ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        WHERE ap.sponsor.id = :sponsorId
        ORDER BY ap.start DESC
        """
    )
    fun findPreviewProjectionsBySponsorId(
        @Param("sponsorId") sponsorId: Long
    ): List<AssistancePlanPreviewProjection>

    @Query(
        """
        SELECT ap.id as id,
               c.id as clientId,
               i.id as institutionId,
               s.id as sponsorId,
               c.firstName as clientFirstname,
               c.lastName as clientLastname,
               i.name as institutionName,
               s.name as sponsorName,
               ap.start as start,
               ap.end as end
        FROM Employee e
        JOIN e.assistancePlanFavorites ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        WHERE e.id = :employeeId
        ORDER BY ap.start DESC
        """
    )
    fun findFavoritePreviewProjectionsByEmployeeId(
        @Param("employeeId") employeeId: Long
    ): List<AssistancePlanPreviewProjection>

    @Query(
        """
        SELECT ap.id
        FROM Employee e
        JOIN e.assistancePlanFavorites ap
        WHERE e.id = :employeeId
        """
    )
    fun findFavoriteAssistancePlanIdsByEmployeeId(
        @Param("employeeId") employeeId: Long
    ): List<Long>

    @Query(
        """
        SELECT aph.assistancePlan.id as assistancePlanId,
               aph.weeklyMinutes as weeklyMinutes
        FROM AssistancePlanHour aph
        WHERE aph.assistancePlan.id in :assistancePlanIds
        """
    )
    fun findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(
        @Param("assistancePlanIds") assistancePlanIds: List<Long>
    ): List<AssistancePlanWeeklyMinutesProjection>

    @Query(
        """
        SELECT g.assistancePlan.id as assistancePlanId,
               gh.weeklyMinutes as weeklyMinutes
        FROM GoalHour gh
        JOIN gh.goal g
        WHERE g.assistancePlan.id in :assistancePlanIds
        """
    )
    fun findWeeklyMinutesFromGoalHoursByAssistancePlanIds(
        @Param("assistancePlanIds") assistancePlanIds: List<Long>
    ): List<AssistancePlanWeeklyMinutesProjection>
}
