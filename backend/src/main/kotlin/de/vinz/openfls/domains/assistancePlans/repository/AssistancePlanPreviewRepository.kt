package de.vinz.openfls.domains.assistancePlans.repository

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPeriodDto
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.projection.AssistancePlanExistingProjection
import de.vinz.openfls.domains.assistancePlans.projection.AssistancePlanPreviewProjection
import de.vinz.openfls.domains.assistancePlans.projection.AssistancePlanWeeklyMinutesProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

/** Read model of the assistance plan list: previews, existing plans and the hours needed to rate them. */
interface AssistancePlanPreviewRepository : Repository<AssistancePlan, Long> {

    @Query(
        """
        SELECT ap.id as id,
               ap.start as start,
               ap.end as end,
               s.name as sponsorName,
               c.archived as clientArchived
        FROM AssistancePlan ap
        JOIN ap.client c
        JOIN ap.sponsor s
        WHERE ap.client.id = :clientId
        ORDER BY ap.start DESC
        """
    )
    fun findExistingProjectionsByClientId(
        @Param("clientId") clientId: Long
    ): List<AssistancePlanExistingProjection>

    @Query(
        """
        SELECT ap.id as id,
               c.id as clientId,
               i.id as institutionId,
               s.id as sponsorId,
               c.firstName as clientFirstname,
               c.lastName as clientLastname,
               c.archived as clientArchived,
               i.name as institutionName,
               s.name as sponsorName,
               ap.hourMode as hourMode,
               hc.weeklyMinutesFrom as hourCorridorWeeklyMinutesFrom,
               hc.weeklyMinutesTill as hourCorridorWeeklyMinutesTill,
               ap.start as start,
               ap.end as end
        FROM AssistancePlan ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        LEFT JOIN ap.hourCorridor hc
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
               c.archived as clientArchived,
               i.name as institutionName,
               s.name as sponsorName,
               ap.hourMode as hourMode,
               hc.weeklyMinutesFrom as hourCorridorWeeklyMinutesFrom,
               hc.weeklyMinutesTill as hourCorridorWeeklyMinutesTill,
               ap.start as start,
               ap.end as end
        FROM AssistancePlan ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        LEFT JOIN ap.hourCorridor hc
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
               c.archived as clientArchived,
               i.name as institutionName,
               s.name as sponsorName,
               ap.hourMode as hourMode,
               hc.weeklyMinutesFrom as hourCorridorWeeklyMinutesFrom,
               hc.weeklyMinutesTill as hourCorridorWeeklyMinutesTill,
               ap.start as start,
               ap.end as end
        FROM AssistancePlan ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        LEFT JOIN ap.hourCorridor hc
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
               c.archived as clientArchived,
               i.name as institutionName,
               s.name as sponsorName,
               ap.hourMode as hourMode,
               hc.weeklyMinutesFrom as hourCorridorWeeklyMinutesFrom,
               hc.weeklyMinutesTill as hourCorridorWeeklyMinutesTill,
               ap.start as start,
               ap.end as end
        FROM Employee e
        JOIN e.assistancePlanFavorites ap
        JOIN ap.client c
        JOIN ap.institution i
        JOIN ap.sponsor s
        LEFT JOIN ap.hourCorridor hc
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
        SELECT new de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPeriodDto(
            ap.id, c.id, ap.start, ap.end)
        FROM AssistancePlan ap
        JOIN ap.client c
        WHERE c.id IN :clientIds
        """
    )
    fun findPeriodDtosByClientIds(
        @Param("clientIds") clientIds: List<Long>
    ): List<AssistancePlanPeriodDto>

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
