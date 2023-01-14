package de.vinz.openfls.model

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class EmployeeInstitutionRightsKey(
    @Column(name = "employee_Id") var employeeId: Long? = null,
    @Column(name = "institution_Id") var institutionId: Long? = null
) : Serializable