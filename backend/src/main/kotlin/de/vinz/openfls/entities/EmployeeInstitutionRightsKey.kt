package de.vinz.openfls.entities

import java.io.Serializable
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class EmployeeInstitutionRightsKey(
        @Column(name = "employee_Id") var employeeId: Long? = null,
        @Column(name = "institution_Id") var institutionId: Long? = null
) : Serializable