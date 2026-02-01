package de.vinz.openfls.domains.absence

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

@Entity
@Table(name = "absences")
data class Absence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @field:NotNull
    @Column(name = "absence_date")
    var absenceDate: LocalDate = LocalDate.now(),

    @Column(name = "employee_id")
    var employeeId: Long = 0,
)
