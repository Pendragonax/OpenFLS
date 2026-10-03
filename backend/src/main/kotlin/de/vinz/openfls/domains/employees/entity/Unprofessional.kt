package de.vinz.openfls.domains.employees.entity

import de.vinz.openfls.domains.sponsors.entity.Sponsor
import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(name = "unprofessionals")
class Unprofessional(
        @EmbeddedId
        var id: UnprofessionalKey? = null,

        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @MapsId("employeeId")
        @JoinColumn(name = "employee_Id", referencedColumnName = "id")
        var employee: Employee? = null,

        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @MapsId("sponsorId")
        @JoinColumn(name = "sponsor_Id", referencedColumnName = "id")
        var sponsor: Sponsor? = null,

        var end: LocalDate? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Unprofessional) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Unprofessional(employeeId = ${id?.employeeId}, sponsorId = ${id?.sponsorId}, end = $end)"
    }
}
