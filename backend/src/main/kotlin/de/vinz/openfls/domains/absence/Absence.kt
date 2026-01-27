package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.employees.entities.Employee
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

//@Entity
//@Table(name = "services")
data class Absence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @field:NotNull
    var absenceDate: LocalDate = LocalDate.now(),

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "employee_id")
    //var employee: Employee? = null
    val employeeId: Long = 0,
)
