package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ContingentRepository : CrudRepository<Contingent, Long> {

    @Query("SELECT u FROM Contingent u " +
            "WHERE u.institution.id = :institutionId " +
            "AND (" +
            "(u.end = null AND :end > u.start) " +
            "OR (u.end != null AND :end >= u.start AND :start <= u.end)" +
            ")")
    fun findByInstitutionIdAndStartAndEnd(institutionId: Long,
                                          start: LocalDate,
                                          end: LocalDate): List<ContingentProjection>

    @Query("SELECT u FROM Contingent u WHERE u.employee.id = :employeeId")
    fun findAllByEmployeeId(@Param("employeeId") employeeId: Long): List<Contingent>

    @Query("SELECT u FROM Contingent u WHERE u.institution.id = :institutionId")
    fun findAllByInstitutionId(@Param("institutionId") institutionId: Long): List<Contingent>
}