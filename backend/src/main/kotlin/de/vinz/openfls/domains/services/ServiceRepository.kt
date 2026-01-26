package de.vinz.openfls.domains.services

import de.vinz.openfls.domains.services.projections.ServiceCalendarProjection
import de.vinz.openfls.domains.services.projections.ServiceProjection
import de.vinz.openfls.domains.services.projections.ServiceSoloProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface ServiceRepository : CrudRepository<Service, Long> {

    @Query("SELECT u FROM Service u " +
            "WHERE u.assistancePlan.id = :assistancePlanId " +
            "AND u.hourType.id = :hourTypeId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end")
    fun findByAssistancePlanIdAndHourTypeIdAndStartAndEnd(assistancePlanId: Long,
                                                          hourTypeId: Long,
                                                          start: LocalDate,
                                                          end: LocalDate): List<ServiceSoloProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE u.institution.id in :institutionIds " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start")
    fun findProjectionsByInstitutionIdsAndStartAndEnd(institutionIds: List<Long>,
                                                      start: LocalDate,
                                                      end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE u.institution.id = :institutionId " +
            "AND u.employee.id = :employeeId " +
            "AND u.client.id = :clientId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start")
    fun findProjectionsByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(institutionId: Long,
                                                                             employeeId: Long,
                                                                             clientId: Long,
                                                                             start: LocalDate,
                                                                             end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE (:institutionId <= 0 OR u.institution.id = :institutionId) " +
            "AND (:employeeId <= 0 OR u.employee.id = :employeeId) " +
            "AND (:clientId <= 0 OR u.client.id = :clientId) " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start")
    fun findProjectionsBy(institutionId: Long,
                          employeeId: Long,
                          clientId: Long,
                          start: LocalDate,
                          end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE (:institutionId <= 0 OR u.institution.id = :institutionId) " +
            "AND u.institution.id in :institutionIds " +
            "AND (:employeeId <= 0 OR u.employee.id = :employeeId) " +
            "AND (:clientId <= 0 OR u.client.id = :clientId) " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start")
    fun findProjectionsBy(institutionId: Long,
                          institutionIds: List<Long>,
                          employeeId: Long,
                          clientId: Long,
                          start: LocalDate,
                          end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE u.assistancePlan.id = :assistancePlanId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end")
    fun findByAssistancePlanIdAndStartAndEnd(assistancePlanId: Long,
                                             start: LocalDate,
                                             end: LocalDate): List<ServiceSoloProjection>

    @Query("SELECT u FROM Service u WHERE u.institution.id = :institutionId AND cast(u.start as LocalDate) = :date")
    fun findByInstitutionIdAndDate(@Param("institutionId") institutionId: Long,
                                   @Param("date") date: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u WHERE u.institution.id = :institutionId " +
            "AND (cast(u.start as LocalDate) > u.assistancePlan.end OR cast(u.start as LocalDate) < u.assistancePlan.start) " +
            "ORDER BY u.start ASC")
    fun findIllegalByInstitutionId(@Param("institutionId") institutionId: Long): List<ServiceProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE u.institution.id = :institutionId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start")
    fun findByInstitutionIdAndStartAndEnd(institutionId: Long,
                                          start: LocalDate,
                                          end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE u.institution.id in :institutionIds " +
            "AND u.client.id = :clientId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start")
    fun findProjectionsByInstitutionIdsAndClientIdAndStartAndEnd(institutionIds: List<Long>,
                                                                 clientId: Long,
                                                                 start: LocalDate,
                                                                 end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u " +
            "WHERE u.institution.id = :institutionId " +
            "AND u.client.id = :clientId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start")
    fun findByInstitutionIdAndClientIdAndStartAndEnd(institutionId: Long,
                                                     clientId: Long,
                                                     start: LocalDate,
                                                     end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
            "AND (cast(u.start as LocalDate) > u.assistancePlan.end OR cast(u.start as LocalDate) < u.assistancePlan.start) " +
            "ORDER BY u.start ASC")
    fun findIllegalByEmployee(@Param("employeeId") employeeId: Long): List<ServiceProjection>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId AND cast(u.start as LocalDate) = :date")
    fun findByEmployeeAndDate(@Param("employeeId") employeeId: Long,
                              @Param("date") date: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end")
    fun findByEmployeeAndStartAndEnd(@Param("employeeId") clientId: Long,
                                     @Param("start") start: LocalDate,
                                     @Param("end") end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u WHERE u.client.id = :clientId AND cast(u.start as LocalDate) = :date")
    fun findByClientAndDate(@Param("clientId") clientId: Long,
                            @Param("date") date: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.client.id = :clientId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start ASC")
    fun findByClientAndStartAndEnd(@Param("clientId") clientId: Long,
                                   @Param("start") start: LocalDate,
                                   @Param("end") end: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.client.id = :clientId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start ASC")
    fun findSoloProjectionByClientAndStartAndEnd(@Param("clientId") clientId: Long,
                                                 @Param("start") start: LocalDate,
                                                 @Param("end") end: LocalDate): List<ServiceProjection>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end " +
            "ORDER BY u.start ASC")
    fun findServiceCalendarProjection(@Param("employeeId") employeeId: Long,
                                                 @Param("start") start: LocalDate,
                                                 @Param("end") end: LocalDate): List<ServiceCalendarProjection>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId AND " +
            "u.client.id = :clientId AND " +
            "cast(u.start as LocalDate) = :date ORDER BY u.start ASC")
    fun findByEmployeeAndClientAndDate(@Param("employeeId") employeeId: Long,
                                       @Param("clientId") clientId: Long,
                                       @Param("date") date: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
            "AND cast(u.start as LocalDate) <= :end " +
            "AND cast(u.start as LocalDate) >= :start " +
            "ORDER BY u.start ASC")
    fun findByEmployeeAndStartEndDate(@Param("employeeId") employeeId: Long,
                                      @Param("start") start: LocalDate,
                                      @Param("end") end: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND (extract(MONTH from u.start)) = :month " +
            "AND u.hourType.id = :hourTypeId " +
            "AND u.assistancePlan.institution.id = :areaId " +
            "AND u.assistancePlan.sponsor.id = :sponsorId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearAndMonthAndHourTypeIdAndAreaIdAndSponsorId(
            @Param("year") year: Int,
            @Param("month") month: Int,
            @Param("hourTypeId") hourTypeId: Long,
            @Param("areaId") areaId: Long,
            @Param("sponsorId") sponsorId: Long): List<Service>

    fun findServicesByAssistancePlanIdAndStartIsBetween(@Param("assistancePlanId") assistancePlanId: Long,
                                                        @Param("start") start: LocalDateTime,
                                                        @Param("end") end: LocalDateTime): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND (extract(MONTH from u.start)) = :month " +
            "AND u.hourType.id = :hourTypeId " +
            "AND u.assistancePlan.sponsor.id = :sponsorId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearAndMonthAndHourTypeIdAndSponsorId(@Param("year") year: Int,
                                                           @Param("month") month: Int,
                                                           @Param("hourTypeId") hourTypeId: Long,
                                                           @Param("sponsorId") sponsorId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND (extract(MONTH from u.start)) = :month " +
            "AND u.hourType.id = :hourTypeId " +
            "AND u.assistancePlan.institution.id = :areaId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearAndMonthAndHourTypeIdAndAreaId(@Param("year") year: Int,
                                                        @Param("month") month: Int,
                                                        @Param("hourTypeId") hourTypeId: Long,
                                                        @Param("areaId") areaId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND (extract(MONTH from u.start)) = :month " +
            "AND u.hourType.id = :hourTypeId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearAndMonthAndHourTypeId(@Param("year") year: Int,
                                               @Param("month") month: Int,
                                               @Param("hourTypeId") hourTypeId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND u.hourType.id = :hourTypeId " +
            "AND u.assistancePlan.institution.id = :areaId " +
            "AND u.assistancePlan.sponsor.id = :sponsorId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearByHourTypeIdAndAreaIdAndSponsorId(@Param("year") year: Int,
                                                           @Param("hourTypeId") hourTypeId: Long,
                                                           @Param("areaId") areaId: Long,
                                                           @Param("sponsorId") sponsorId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND u.hourType.id = :hourTypeId " +
            "AND u.assistancePlan.sponsor.id = :sponsorId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearByHourTypeIdAndSponsorId(@Param("year") year: Int,
                                                  @Param("hourTypeId") hourTypeId: Long,
                                                  @Param("sponsorId") sponsorId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND u.hourType.id = :hourTypeId " +
            "AND u.assistancePlan.institution.id = :areaId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearByHourTypeIdAndAreaId(@Param("year") year: Int,
                                               @Param("hourTypeId") hourTypeId: Long,
                                               @Param("areaId") areaId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "AND u.hourType.id = :hourTypeId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearByHourTypeId(@Param("year") year: Int,
                                      @Param("hourTypeId") hourTypeId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE " +
            "(extract(YEAR from u.start)) = :year " +
            "ORDER BY u.start ASC")
    fun findServiceByYear(@Param("year") year: Int): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId")
    fun findByEmployee(@Param("employeeId") employeeId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE u.assistancePlan.id = :assistancePlanId")
    fun findByAssistancePlan(@Param("assistancePlanId") assistancePlanId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE u.assistancePlan.id = :assistancePlanId " +
            "AND (cast(u.start as LocalDate) > u.assistancePlan.end OR cast(u.start as LocalDate) < u.assistancePlan.start) " +
            "ORDER BY u.start ASC")
    fun findIllegalByAssistancePlan(@Param("assistancePlanId") assistancePlanId: Long): List<ServiceProjection>

    @Query("SELECT u FROM Service u WHERE u.assistancePlan.id = :assistancePlanId " +
            "AND (cast(u.start as LocalDate) > :end OR cast(u.start as LocalDate) < :start) " +
            "ORDER BY u.start ASC")
    fun findByAssistancePlanAndNotBetweenStartAndEnd(@Param("assistancePlanId") assistancePlanId: Long,
                                                     @Param("start") start: LocalDate,
                                                     @Param("end") end: LocalDate): List<ServiceProjection>

    @Query("SELECT Count(*) FROM Service u WHERE u.employee.id = :employeeId")
    fun countByEmployeeId(@Param("employeeId") employeeId: Long): Long

    @Query("SELECT Count(*) FROM Service u WHERE u.client.id = :clientId")
    fun countByClientId(@Param("clientId") clientId: Long): Long

    @Query("SELECT Count(*) FROM Service u WHERE u.assistancePlan.id = :assistancePlanId")
    fun countByAssistancePlanId(@Param("assistancePlanId") assistancePlanId: Long): Long

    @Query("SELECT Count(*) FROM Service u JOIN u.goals g WHERE g.id = :goalId")
    fun countByGoalId(@Param("goalId") goalId: Long): Long

}