package de.vinz.openfls.domains.permissions

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import jakarta.persistence.*


@Entity
@Table(name = "permissions")
class Permission(
        @EmbeddedId
        var id: EmployeeInstitutionRightsKey = EmployeeInstitutionRightsKey(),

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @MapsId("employeeId")
        @JoinColumn(name = "employee_Id", referencedColumnName = "id")
        var employee: Employee? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @MapsId("institutionId")
        @JoinColumn(name = "institution_Id", referencedColumnName = "id")
        var institution: Institution? = null,

        var readEntries: Boolean = false,
        var writeEntries: Boolean = false,
        var changeInstitution: Boolean = false,
        var affiliated: Boolean = false
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Permission) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id.hashCode() ?: 0
        }
}