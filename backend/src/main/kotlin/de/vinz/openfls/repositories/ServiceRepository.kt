package de.vinz.openfls.repositories

import de.vinz.openfls.model.Service
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ServiceRepository : CrudRepository<Service, Long> {
    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId AND cast(u.start as LocalDate) = :date")
    fun findByEmployeeAndDate(@Param("employeeId") employeeId: Long,
                              @Param("date") date: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId " +
            "AND cast(u.start as LocalDate) >= :start " +
            "AND cast(u.start as LocalDate) <= :end")
    fun findByEmployeeAndStartAndEnd(@Param("employeeId") clientId: Long,
                                   @Param("start") start: LocalDate,
                                   @Param("end") end: LocalDate): List<Service>

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
            "AND u.hourType.id = :hourTypeId " +
            "AND u.assistancePlan.institution.id = :areaId " +
            "AND u.assistancePlan.sponsor.id = :sponsorId " +
            "ORDER BY u.start ASC")
    fun findServiceByYearByHourTypeIdAndAreaIdAndSponsorId(@Param("year") year: Int,
                                                           @Param("hourTypeId") hourTypeId: Long,
                                                           @Param("areaId") areaId: Long,
                                                           @Param("sponsorId") sponsorId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId")
    fun findByEmployee(@Param("employeeId") employeeId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE u.assistancePlan.id = :assistancePlanId")
    fun findByAssistancePlan(@Param("assistancePlanId") assistancePlanId: Long): List<Service>

    @Query("SELECT Count(*) FROM Service u WHERE u.employee.id = :employeeId")
    fun countByEmployeeId(@Param("employeeId") employeeId: Long): Long

    @Query("SELECT Count(*) FROM Service u WHERE u.client.id = :clientId")
    fun countByClientId(@Param("clientId") clientId: Long): Long

    @Query("SELECT Count(*) FROM Service u WHERE u.assistancePlan.id = :assistancePlanId")
    fun countByAssistancePlanId(@Param("assistancePlanId") assistancePlanId: Long): Long

    @Query("SELECT Count(*) FROM Service u JOIN u.goals g WHERE g.id = :goalId")
    fun countByGoalId(@Param("goalId") goalId: Long): Long

}