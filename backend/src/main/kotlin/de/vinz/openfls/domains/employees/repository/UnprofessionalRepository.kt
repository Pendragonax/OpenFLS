package de.vinz.openfls.domains.employees.repository

import de.vinz.openfls.domains.employees.entity.Unprofessional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface UnprofessionalRepository: CrudRepository<Unprofessional, Long> {

    @Query("SELECT u FROM Unprofessional u WHERE u.employee.id = :employeeId")
    fun findByEmployeeId(@Param("employeeId") employeeId: Long): List<Unprofessional>

    @Modifying
    @Query("DELETE FROM Unprofessional u WHERE u.employee.id = :employeeId AND u.sponsor.id = :sponsorId")
    fun deleteByEmployeeIdAndSponsorId(@Param("employeeId") employeeId: Long,
                                    @Param("sponsorId") sponsorId: Long)
}