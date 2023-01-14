package de.vinz.openfls.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*


@Entity
@Table(name = "permissions")
class Permission(
    @EmbeddedId
    var id: EmployeeInstitutionRightsKey,

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
    @MapsId("institutionId")
    @JoinColumn(name = "institution_Id", referencedColumnName = "id")
    var institution: Institution?,

    var readEntries: Boolean,

    var writeEntries: Boolean,

    var changeInstitution: Boolean,

    var affiliated: Boolean
    )