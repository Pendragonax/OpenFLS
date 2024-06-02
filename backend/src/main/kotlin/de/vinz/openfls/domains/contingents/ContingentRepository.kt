package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ContingentRepository : CrudRepository<Contingent, Long> {

    @Query("""
        SELECT u FROM Contingent u
        WHERE (u.institution IS NOT NULL AND u.institution.id = :institutionId)
        AND (
            (u.end IS NULL AND :end > u.start)
            OR (u.end IS NOT NULL AND :end >= u.start AND :start <= u.end)
        )
    """)
    fun findByInstitutionIdAndStartAndEnd(
            @Param("institutionId") institutionId: Long,
            @Param("start") start: LocalDate,
            @Param("end") end: LocalDate): List<ContingentProjection>

    @Query("SELECT u FROM Contingent u WHERE u.employee.id = :employeeId")
    fun findAllByEmployeeId(@Param("employeeId") employeeId: Long): List<Contingent>

    @Query("SELECT u FROM Contingent u WHERE u.institution.id = :institutionId")
    fun findAllByInstitutionId(@Param("institutionId") institutionId: Long): List<Contingent>
}