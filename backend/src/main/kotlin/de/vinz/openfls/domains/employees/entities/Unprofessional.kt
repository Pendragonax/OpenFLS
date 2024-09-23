package de.vinz.openfls.domains.employees.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto
import de.vinz.openfls.domains.sponsors.Sponsor
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

    companion object {
        fun from(unprofessionalDto: UnprofessionalDto): Unprofessional {
            return Unprofessional(
                    id = UnprofessionalKey(
                            employeeId = unprofessionalDto.employeeId,
                            sponsorId = unprofessionalDto.sponsorId),
                    end = unprofessionalDto.end
            )
        }
    }
}