package de.vinz.openfls.domains.services.repository

import de.vinz.openfls.domains.services.dto.ClientLatestServiceResponse
import de.vinz.openfls.domains.services.entity.Service
import de.vinz.openfls.domains.services.projection.ContingentEvaluationServiceProjection
import de.vinz.openfls.domains.services.projection.ServiceTimeSlotProjection
import de.vinz.openfls.domains.services.projection.AssistancePlanServiceMinutesProjection
import de.vinz.openfls.domains.services.projection.ServiceCalendarProjection
import de.vinz.openfls.domains.services.projection.ServiceProjection
import de.vinz.openfls.domains.services.projection.ServiceWithRelationsProjection
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface ServiceRepository : CrudRepository<Service, Long> {

    @Query(
        "SELECT u FROM Service u " +
                "WHERE u.assistancePlan.id = :assistancePlanId " +
                "AND u.hourType.id = :hourTypeId " +
                "AND cast(u.start as LocalDate) >= :start " +
                "AND cast(u.start as LocalDate) <= :end"
    )
    fun findByAssistancePlanIdAndHourTypeIdAndStartAndEnd(
        assistancePlanId: Long,
        hourTypeId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ServiceProjection>

    @Query(
        "SELECT u.id as id, u.start as start, u.minutes as minutes, u.employee.id as employeeId FROM Service u " +
                "WHERE u.institution.id = :institutionId " +
                "AND cast(u.start as LocalDate) >= :start " +
                "AND cast(u.start as LocalDate) <= :end " +
                "ORDER BY u.start"
    )
    fun findContingentEvaluationServicesByInstitutionIdAndStartAndEnd(
        institutionId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ContingentEvaluationServiceProjection>

    @Query(
        "SELECT u FROM Service u " +
                "WHERE (:institutionId <= 0 OR u.institution.id = :institutionId) " +
                "AND u.institution.id in :institutionIds " +
                "AND (:employeeId <= 0 OR u.employee.id = :employeeId) " +
                "AND (:clientId <= 0 OR u.client.id = :clientId) " +
                "AND cast(u.start as LocalDate) >= :start " +
                "AND cast(u.start as LocalDate) <= :end " +
                "ORDER BY u.start"
    )
    fun findWithRelationsByFilter(
        institutionId: Long,
        institutionIds: List<Long>,
        employeeId: Long,
        clientId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ServiceWithRelationsProjection>

    @Query(
        "SELECT u.id AS id, u.start AS start, u.end AS end, e.firstname AS employeeFirstname, e.lastname AS employeeLastname " +
                "FROM Service u JOIN u.employee e " +
                "WHERE u.client.id = :clientId " +
                "AND cast(u.start as LocalDate) = :date " +
                "ORDER BY u.start"
    )
    fun findServiceTimeSlotProjectionByClientIdAndStartIsBetween(
        clientId: Long,
        date: LocalDate,
    ): List<ServiceTimeSlotProjection>

    @Query(
        "SELECT u FROM Service u " +
                "WHERE u.assistancePlan.id = :assistancePlanId " +
                "AND cast(u.start as LocalDate) >= :start " +
                "AND cast(u.start as LocalDate) <= :end"
    )
    fun findByAssistancePlanIdAndStartAndEnd(
        assistancePlanId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ServiceProjection>

    @Query(
        """
        SELECT u.assistancePlan.id as assistancePlanId,
               u.minutes as minutes
        FROM Service u
        WHERE u.assistancePlan.id in :assistancePlanIds
          AND cast(u.start as LocalDate) >= :start
          AND cast(u.start as LocalDate) <= :end
          AND cast(u.start as LocalDate) >= u.assistancePlan.start
          AND cast(u.start as LocalDate) <= u.assistancePlan.end
        """
    )
    fun findMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd(
        assistancePlanIds: List<Long>,
        start: LocalDate,
        end: LocalDate
    ): List<AssistancePlanServiceMinutesProjection>

    @Query(
        """
        SELECT u.assistancePlan.id as assistancePlanId,
               u.minutes as minutes
        FROM Service u
        WHERE u.assistancePlan.id in :assistancePlanIds
          AND cast(u.start as LocalDate) >= u.assistancePlan.start
          AND cast(u.start as LocalDate) <= u.assistancePlan.end
          AND cast(u.start as LocalDate) <= :until
        """
    )
    fun findMinutesInPlanPeriodByAssistancePlanIdsUntil(
        @Param("assistancePlanIds") assistancePlanIds: List<Long>,
        @Param("until") until: LocalDate
    ): List<AssistancePlanServiceMinutesProjection>

    @Query(
        "SELECT u FROM Service u WHERE u.institution.id = :institutionId " +
                "AND (cast(u.start as LocalDate) > u.assistancePlan.end OR cast(u.start as LocalDate) < u.assistancePlan.start) " +
                "ORDER BY u.start ASC"
    )
    fun findOutsideAssistancePlanPeriodByInstitutionId(@Param("institutionId") institutionId: Long): List<ServiceWithRelationsProjection>

    @Query(
        "SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
                "AND (cast(u.start as LocalDate) > u.assistancePlan.end OR cast(u.start as LocalDate) < u.assistancePlan.start) " +
                "ORDER BY u.start ASC"
    )
    fun findOutsideAssistancePlanPeriodByEmployeeId(@Param("employeeId") employeeId: Long): List<ServiceWithRelationsProjection>

    @Query(
        "SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
                "AND cast(u.start as LocalDate) >= :start " +
                "AND cast(u.start as LocalDate) <= :end"
    )
    fun findByEmployeeAndStartAndEnd(
        @Param("employeeId") clientId: Long,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<ServiceWithRelationsProjection>

    fun findByClientIdOrderByStartAsc(clientId: Long): List<Service>

    @Query(
        """
        SELECT new de.vinz.openfls.domains.services.dto.ClientLatestServiceResponse(
            s.id, s.start, s.end, s.minutes, s.title, s.content,
            i.id, i.name,
            e.id, e.firstname, e.lastname,
            ap.id)
        FROM Service s
        JOIN s.institution i
        JOIN s.employee e
        JOIN s.assistancePlan ap
        WHERE s.client.id = :clientId
        AND (:isAdmin = true OR e.id = :employeeId OR i.id IN :readableInstitutionIds)
        ORDER BY s.start DESC, s.id DESC
        """
    )
    fun findLatestByClientId(
        @Param("clientId") clientId: Long,
        @Param("employeeId") employeeId: Long,
        @Param("readableInstitutionIds") readableInstitutionIds: List<Long>,
        @Param("isAdmin") isAdmin: Boolean,
        pageable: Pageable
    ): List<ClientLatestServiceResponse>

    @Query(
        "SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
                "AND cast(u.start as LocalDate) >= :start " +
                "AND cast(u.start as LocalDate) <= :end " +
                "ORDER BY u.start ASC"
    )
    fun findServiceCalendarProjection(
        @Param("employeeId") employeeId: Long,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<ServiceCalendarProjection>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND (extract(MONTH from u.start)) = :month " +
                "AND u.hourType.id = :hourTypeId " +
                "AND u.assistancePlan.institution.id = :institutionId " +
                "AND u.assistancePlan.sponsor.id = :sponsorId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("hourTypeId") hourTypeId: Long,
        @Param("institutionId") institutionId: Long,
        @Param("sponsorId") sponsorId: Long
    ): List<Service>

    fun findServicesByAssistancePlanIdAndStartIsBetween(
        @Param("assistancePlanId") assistancePlanId: Long,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Service>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND (extract(MONTH from u.start)) = :month " +
                "AND u.hourType.id = :hourTypeId " +
                "AND u.assistancePlan.sponsor.id = :sponsorId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndMonthAndHourTypeIdAndSponsorId(
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("hourTypeId") hourTypeId: Long,
        @Param("sponsorId") sponsorId: Long
    ): List<Service>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND (extract(MONTH from u.start)) = :month " +
                "AND u.hourType.id = :hourTypeId " +
                "AND u.assistancePlan.institution.id = :institutionId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndMonthAndHourTypeIdAndInstitutionId(
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("hourTypeId") hourTypeId: Long,
        @Param("institutionId") institutionId: Long
    ): List<Service>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND (extract(MONTH from u.start)) = :month " +
                "AND u.hourType.id = :hourTypeId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndMonthAndHourTypeId(
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("hourTypeId") hourTypeId: Long
    ): List<Service>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND u.hourType.id = :hourTypeId " +
                "AND u.assistancePlan.institution.id = :institutionId " +
                "AND u.assistancePlan.sponsor.id = :sponsorId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndHourTypeIdAndInstitutionIdAndSponsorId(
        @Param("year") year: Int,
        @Param("hourTypeId") hourTypeId: Long,
        @Param("institutionId") institutionId: Long,
        @Param("sponsorId") sponsorId: Long
    ): List<Service>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND u.hourType.id = :hourTypeId " +
                "AND u.assistancePlan.sponsor.id = :sponsorId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndHourTypeIdAndSponsorId(
        @Param("year") year: Int,
        @Param("hourTypeId") hourTypeId: Long,
        @Param("sponsorId") sponsorId: Long
    ): List<Service>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND u.hourType.id = :hourTypeId " +
                "AND u.assistancePlan.institution.id = :institutionId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndHourTypeIdAndInstitutionId(
        @Param("year") year: Int,
        @Param("hourTypeId") hourTypeId: Long,
        @Param("institutionId") institutionId: Long
    ): List<Service>

    @Query(
        "SELECT u FROM Service u WHERE " +
                "(extract(YEAR from u.start)) = :year " +
                "AND u.hourType.id = :hourTypeId " +
                "ORDER BY u.start ASC"
    )
    fun findAllByYearAndHourTypeId(
        @Param("year") year: Int,
        @Param("hourTypeId") hourTypeId: Long
    ): List<Service>

    @Query("SELECT u FROM Service u WHERE u.assistancePlan.id = :assistancePlanId")
    fun findAllByAssistancePlanId(@Param("assistancePlanId") assistancePlanId: Long): List<Service>

    @Query("SELECT Count(*) FROM Service u WHERE u.employee.id = :employeeId")
    fun countByEmployeeId(@Param("employeeId") employeeId: Long): Long

    @Query("SELECT Count(*) FROM Service u WHERE u.client.id = :clientId")
    fun countByClientId(@Param("clientId") clientId: Long): Long

    @Query("SELECT Count(*) FROM Service u WHERE u.assistancePlan.id = :assistancePlanId")
    fun countByAssistancePlanId(@Param("assistancePlanId") assistancePlanId: Long): Long
}
