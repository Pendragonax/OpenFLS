package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.employees.entities.Employee
import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(name = "unprofessionals")
class Unprofessional(
        @EmbeddedId
        var id: UnprofessionalKey? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @MapsId("employeeId")
        @JoinColumn(name = "employee_Id", referencedColumnName = "id")
        var employee: Employee? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @MapsId("sponsorId")
        @JoinColumn(name = "sponsor_Id", referencedColumnName = "id")
        var sponsor: Sponsor? = null,

        var end: LocalDate? = null
) {
    override fun toString(): String {
        return "Unprofessional(employeeId = ${id?.employeeId}, sponsorId = ${id?.sponsorId}, end = $end)"
    }
}