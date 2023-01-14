package de.vinz.openfls.repositories

import de.vinz.openfls.model.Contingent
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ContingentRepository : CrudRepository<Contingent, Long> {

    @Query("SELECT u FROM Contingent u WHERE u.employee.id = :employeeId")
    fun findAllByEmployeeId(@Param("employeeId") employeeId: Long): List<Contingent>

    @Query("SELECT u FROM Contingent u WHERE u.institution.id = :institutionId")
    fun findAllByInstitutionId(@Param("institutionId") institutionId: Long): List<Contingent>
}