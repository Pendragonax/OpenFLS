package de.vinz.openfls.domains.absence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface AbsenceRepository : JpaRepository<Absence, Long> {

    fun findByEmployeeIdAndAbsenceDate(employeeId: Long, absenceDate: LocalDate): Absence?

    fun findAllByEmployeeId(employeeId: Long): List<Absence>

    fun findAllByAbsenceDateBetween(start: LocalDate, end: LocalDate): List<Absence>
}
