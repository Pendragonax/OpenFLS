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

    @Query("SELECT u FROM Service u WHERE u.client.id = :clientId AND cast(u.start as LocalDate) = :date")
    fun findByClientAndDate(@Param("clientId") clientId: Long,
                              @Param("date") date: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId AND " +
            "u.client.id = :clientId AND " +
            "cast(u.start as LocalDate) = :date")
    fun findByEmployeeAndClientAndDate(@Param("employeeId") employeeId: Long,
                                       @Param("clientId") clientId: Long,
                                       @Param("date") date: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId AND " +
            "cast(u.start as LocalDate) <= :end AND " +
            "cast(u.start as LocalDate) >= :start")
    fun findByEmployeeAndStartEndDate(@Param("employeeId") employeeId: Long,
                                       @Param("start") start: LocalDate,
                                       @Param("end") end: LocalDate): List<Service>

    @Query("SELECT u FROM Service u WHERE u.employee.id = :employeeId")
    fun findByEmployee(@Param("employeeId") employeeId: Long): List<Service>

    @Query("SELECT u FROM Service u WHERE u.assistancePlan.id = :assistancePlanId")
    fun findByAssistancePlan(@Param("assistancePlanId") assistancePlanId: Long): List<Service>

}