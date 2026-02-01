package de.vinz.openfls.domains.absence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AbsenceRepository : JpaRepository<Absence, Long> {

    fun findByEmployeeIdAndAbsenceDate(employeeId: Long, absenceDate: LocalDate): Absence?

    fun findAllByEmployeeId(employeeId: Long): List<Absence>

    @Query("SELECT a FROM Absence a WHERE YEAR(a.absenceDate) = :year")
    fun findAllByYear(@Param("year") year: Int): List<Absence>
}
