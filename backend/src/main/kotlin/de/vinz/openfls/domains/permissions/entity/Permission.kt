package de.vinz.openfls.domains.permissions.entity

import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.institutions.entity.Institution
import jakarta.persistence.*

@Entity
@Table(name = "permissions")
class Permission(
        @EmbeddedId
        var id: PermissionKey = PermissionKey(),

        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @MapsId("employeeId")
        @JoinColumn(name = "employee_Id", referencedColumnName = "id")
        var employee: Employee? = null,

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
                return id.hashCode()
        }
}
