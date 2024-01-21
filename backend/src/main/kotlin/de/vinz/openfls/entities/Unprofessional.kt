package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "unprofessionals")
class Unprofessional(
    @EmbeddedId
    var id: UnprofessionalKey,

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @MapsId("employeeId")
    @JoinColumn(name = "employee_Id", referencedColumnName = "id")
    var employee: Employee?,

    @JsonIgnore
    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @MapsId("sponsorId")
    @JoinColumn(name = "sponsor_Id", referencedColumnName = "id")
    var sponsor: Sponsor?,

    var end: LocalDate? = null
) {
    override fun toString(): String {
        return "employeeId = ${id.employeeId} | sponsorId = ${id.sponsorId} | end = $end | sponsor = $sponsor | employee = $employee"
    }
}