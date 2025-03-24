package de.vinz.openfls.domains.contingents

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.institutions.Institution
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

@Entity
@Table(name = "contingents")
class Contingent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @field:NotNull(message = "Start is required.")
    var start: LocalDate = LocalDate.now(),

    var end: LocalDate? = null,

    @field:NotNull(message = "Weekly service hours are required.")
    var weeklyServiceHours: Double = 0.0,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    var employee: Employee? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    var institution: Institution? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contingent) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        fun from(contingent: ContingentDto): Contingent {
            return Contingent(
                id = contingent.id,
                start = contingent.start,
                end = contingent.end,
                weeklyServiceHours = contingent.weeklyServiceHours,
                employee = Employee(id = contingent.employeeId),
                institution = Institution(id = contingent.institutionId)
            )
        }
    }
}