package de.vinz.openfls.domains.contingents

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.entities.Employee
import de.vinz.openfls.entities.Institution
import java.time.LocalDate
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "contingents")
class Contingent(
        @Id
        @GeneratedValue(strategy= GenerationType.IDENTITY)
        var id: Long? = null,

        @field:NotNull(message = "start needed")
        var start: LocalDate = LocalDate.now(),

        var end: LocalDate? = null,

        @field:NotNull(message = "weekly hours needed")
        @Column(precision = 7, scale = 2)
        var weeklyServiceHours: Double = 0.0,

        @JsonIgnore
        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "employee_id")
        var employee: Employee,

        @JsonIgnore
        @ManyToOne(
                cascade = [CascadeType.PERSIST],
                fetch = FetchType.LAZY
        )
        @JoinColumn(name = "institution_id")
        var institution: Institution) {
}