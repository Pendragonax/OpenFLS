package de.vinz.openfls.domains.contingents

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.institutions.Institution
import java.time.LocalDate
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "contingents")
class Contingent(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @field:NotNull(message = "Start is required.")
        var start: LocalDate = LocalDate.now(),

        var end: LocalDate? = null,

        @field:NotNull(message = "Weekly service hours are required.")
        var weeklyServiceHours: Double = 0.0,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_id")
        var employee: Employee? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "institution_id")
        var institution: Institution? = null
)